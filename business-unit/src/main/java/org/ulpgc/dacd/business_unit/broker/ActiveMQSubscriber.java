package org.ulpgc.dacd.business_unit.broker;

import org.ulpgc.dacd.business_unit.model.WeatherEvent;
import org.ulpgc.dacd.business_unit.model.SatelliteEvent;
import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.business_unit.repository.SQLiteDataMart;

import javax.jms.*;

public class ActiveMQSubscriber {
    private static final String CLIENT_ID = "BusinessUnit-Node1";
    private static final String SPACEX_TOPIC = "sensor.SpaceX";
    private static final String WEATHER_TOPIC = "prediction.Weather";

    private final Gson gson = new Gson();
    private final SQLiteDataMart dataMart;

    public ActiveMQSubscriber(SQLiteDataMart dataMart) {
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
        String brokerUrl = System.getenv("ACTIVEMQ_URL");
        if (brokerUrl == null || brokerUrl.isEmpty()) {
            brokerUrl = "failover:(tcp://localhost:61616)";
        }
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
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
            String topicName = message.getJMSDestination().toString().replace("topic://", "");

            if (topicName.equals(SPACEX_TOPIC)) {
                SatelliteEvent sat = gson.fromJson(json, SatelliteEvent.class);
                dataMart.addSatellite(sat);
            } else if (topicName.equals(WEATHER_TOPIC)) {
                WeatherEvent weather = gson.fromJson(json, WeatherEvent.class);
                dataMart.addWeather(weather);
            }
        } catch (JMSException e) {
            System.err.println("Error procesando mensaje: " + e.getMessage());
        }
    }
}