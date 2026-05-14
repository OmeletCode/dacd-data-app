package org.ulpgc.starlink.eventstore.control;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveMQSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(ActiveMQSubscriber.class);
    private static final String DEFAULT_URL = "tcp://localhost:61616";
    private static final String CLIENT_ID = "EventStoreBuilder-Node1";

    public void start() {
        FileEventStore eventStore = new FileEventStore();

        try {
            String brokerUrl = System.getenv("ACTIVEMQ_URL");
            if (brokerUrl == null) brokerUrl = DEFAULT_URL;

            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            Connection connection = connectionFactory.createConnection();
            connection.setClientID(CLIENT_ID);
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic spaceXTopic = session.createTopic("sensor.SpaceX");
            MessageConsumer spaceXConsumer = session.createDurableSubscriber(spaceXTopic, "SpaceX-Sub");        

            Topic weatherTopic = session.createTopic("prediction.Weather");
            MessageConsumer weatherConsumer = session.createDurableSubscriber(weatherTopic, "Weather-Sub");     

            MessageListener listener = message -> {
                try {
                    if (message instanceof TextMessage textMessage) {
                        String json = textMessage.getText();
                        String topicName = message.getJMSDestination().toString().replace("topic://", "");  
                        eventStore.save(topicName, json);
                    }
                } catch (JMSException e) {
                    logger.error("Error al leer el mensaje: {}", e.getMessage());
                }
            };

            spaceXConsumer.setMessageListener(listener);
            weatherConsumer.setMessageListener(listener);

            logger.info("✅ Event Store Builder conectado a {} y esperando mensajes...", brokerUrl);

        } catch (JMSException e) {
            logger.error("Error conectando a ActiveMQ: {}", e.getMessage());
        }
    }
}
