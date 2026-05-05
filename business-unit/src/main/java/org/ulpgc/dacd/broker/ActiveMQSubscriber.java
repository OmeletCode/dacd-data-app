package org.ulpgc.dacd.broker;

import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.model.SatelliteEvent;
import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.repository.MemoryDataMart;

import javax.jms.*;

public class ActiveMQSubscriber {
    private static final String URL = "failover:(tcp://localhost:61616)";
    private static final String CLIENT_ID = "BusinessUnit-Node1";
    private static final String SPACEX_TOPIC = "sensor.SpaceX";
    private static final String WEATHER_TOPIC = "prediction.Weather";

    private final Gson gson = new Gson();
    private final MemoryDataMart dataMart;

    public ActiveMQSubscriber(MemoryDataMart dataMart) {
        this.dataMart = dataMart;
    }

    public void start() {
        try {
            Connection connection = createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            setupSubscriber(session, SPACEX_TOPIC, "SpaceX-BU-Sub");
            setupSubscriber(session, WEATHER_TOPIC, "Weather-BU-Sub");

            System.out.println("📡 Business Unit escuchando ActiveMQ en tiempo real...");

        } catch (JMSException e) {
            System.err.println("Error en la conexión ActiveMQ: " + e.getMessage());
        }
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
        Connection connection = connectionFactory.createConnection();
        connection.setClientID(CLIENT_ID);
        connection.start();
        return connection;
    }

    private void setupSubscriber(Session session, String topicName, String subscriberName) throws JMSException {
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createDurableSubscriber(topic, subscriberName);
        consumer.setMessageListener(this::processMessage);
    }

    private void processMessage(Message message) {
        try {
            if (!(message instanceof TextMessage textMessage)) return;

            String json = textMessage.getText();
            String topicName = extractTopicName(message);

            routeMessageToDataMart(topicName, json);

        } catch (JMSException e) {
            System.err.println("Error procesando mensaje: " + e.getMessage());
        }
    }

    private String extractTopicName(Message message) throws JMSException {
        return message.getJMSDestination().toString().replace("topic://", "");
    }

    private void routeMessageToDataMart(String topicName, String json) {
        if (topicName.equals(SPACEX_TOPIC)) {
            processSatelliteEvent(json);
        } else if (topicName.equals(WEATHER_TOPIC)) {
            processWeatherEvent(json);
        }
    }

    private void processSatelliteEvent(String json) {
        SatelliteEvent sat = gson.fromJson(json, SatelliteEvent.class);
        dataMart.addSatellite(sat);
        System.out.println("🛰️ Satélite guardado en memoria: " + sat.id());
    }

    private void processWeatherEvent(String json) {
        WeatherEvent weather = gson.fromJson(json, WeatherEvent.class);
        dataMart.addWeather(weather);
        System.out.println("☁️ Clima guardado en memoria: " + weather.locationName());
    }
}