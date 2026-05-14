package org.ulpgc.starlink.monitor.control;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class ActiveMQSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(ActiveMQSubscriber.class);
    private static final String CLIENT_ID = "BusinessUnit-Node1";
    private static final String SPACEX_TOPIC = "sensor.SpaceX";
    private static final String WEATHER_TOPIC = "prediction.Weather";

    private final Gson gson = new Gson();
    private final String brokerUrl;
    private final DataMart dataMart;

    public ActiveMQSubscriber(String brokerUrl, DataMart dataMart) {
        this.brokerUrl = brokerUrl;
        this.dataMart = dataMart;
    }

    public void start() {
        try {
            Connection connection = createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            setupSubscriber(session, SPACEX_TOPIC, "SpaceX-BU-Sub");
            setupSubscriber(session, WEATHER_TOPIC, "Weather-BU-Sub");

            logger.info("📡 Business Unit escuchando ActiveMQ en: {}", this.brokerUrl);
        } catch (JMSException e) {
            logger.error("❌ Error en la conexión ActiveMQ: {}", e.getMessage());
        }
    }

    private Connection createConnection() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.setClientID(CLIENT_ID);
        connection.start();
        return connection;
    }

    private void setupSubscriber(Session session, String topicName, String subName) throws JMSException {
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createDurableSubscriber(topic, subName);

        consumer.setMessageListener(message -> {
            if (message instanceof TextMessage textMessage) {
                try {
                    JsonObject event = gson.fromJson(textMessage.getText(), JsonObject.class);
                    
                    if (event.has("ss") && event.has("ts")) {
                        dataMart.updateServiceHealth(
                            event.get("ss").getAsString().toLowerCase(), 
                            event.get("ts").getAsString()
                        );
                    }
                    
                    dataMart.save(event);
                } catch (JMSException e) {
                    logger.error("Error procesando mensaje: {}", e.getMessage());
                }
            }
        });
    }
}
