package org.ulpgc.dacd.broker;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.model.SatelliteEvent;
import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.repository.MemoryDataMart;

import javax.jms.*;

public class ActiveMQSubscriber {
    private static final String URL = "failover:(tcp://localhost:61616)";
    private static final String CLIENT_ID = "BusinessUnit-Node1";
    private final Gson gson = new Gson();

    // Nuestro almacén en memoria
    private final MemoryDataMart dataMart;

    // Se lo pasamos por el constructor
    public ActiveMQSubscriber(MemoryDataMart dataMart) {
        this.dataMart = dataMart;
    }

    public void start() {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
            Connection connection = connectionFactory.createConnection();
            connection.setClientID(CLIENT_ID);
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic spaceXTopic = session.createTopic("sensor.SpaceX");
            MessageConsumer spaceXConsumer = session.createDurableSubscriber(spaceXTopic, "SpaceX-BU-Sub");

            Topic weatherTopic = session.createTopic("prediction.Weather");
            MessageConsumer weatherConsumer = session.createDurableSubscriber(weatherTopic, "Weather-BU-Sub");

            MessageListener listener = message -> {
                try {
                    if (message instanceof TextMessage textMessage) {
                        String json = textMessage.getText();
                        String topicName = message.getJMSDestination().toString().replace("topic://", "");

                        if (topicName.equals("sensor.SpaceX")) {
                            SatelliteEvent sat = gson.fromJson(json, SatelliteEvent.class);
                            dataMart.addSatellite(sat); // 💾 Guardado en memoria
                            System.out.println("🛰️ Satélite guardado en memoria: " + sat.id());
                        } else if (topicName.equals("prediction.Weather")) {
                            WeatherEvent weather = gson.fromJson(json, WeatherEvent.class);
                            dataMart.addWeather(weather); // 💾 Guardado en memoria
                            System.out.println("☁️ Clima guardado en memoria: " + weather.location());
                        }
                    }
                } catch (JMSException e) {
                    System.err.println("Error procesando mensaje: " + e.getMessage());
                }
            };

            spaceXConsumer.setMessageListener(listener);
            weatherConsumer.setMessageListener(listener);

            System.out.println("📡 Business Unit escuchando ActiveMQ en tiempo real...");

        } catch (JMSException e) {
            System.err.println("Error en la conexión ActiveMQ: " + e.getMessage());
        }
    }
}