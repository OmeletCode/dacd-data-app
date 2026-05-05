package org.ulpgc.dacd;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class ActiveMQSubscriber {
    private static final String BROKER_URL = "failover:(tcp://localhost:61616)";
    private static final String CLIENT_ID = "EventStoreBuilder-Node1";
    private static final String SPACEX_TOPIC = "sensor.SpaceX";
    private static final String WEATHER_TOPIC = "prediction.Weather";

    private final FileEventStore eventStore;

    // Inyectamos la dependencia por el constructor
    public ActiveMQSubscriber(FileEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void start() {
        try {
            Connection connection = createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            setupSubscriber(session, SPACEX_TOPIC, "SpaceX-Sub");
            setupSubscriber(session, WEATHER_TOPIC, "Weather-Sub");

            System.out.println("✅ Event Store Builder conectado y esperando mensajes...");

        } catch (JMSException e) {
            System.err.println("Error conectando a ActiveMQ: " + e.getMessage());
        }
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = connectionFactory.createConnection();
        connection.setClientID(CLIENT_ID);
        connection.start();
        return connection;
    }

    private void setupSubscriber(Session session, String topicName, String subscriptionName) throws JMSException {
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createDurableSubscriber(topic, subscriptionName);
        // Usamos Method Reference para mantener el código limpio
        consumer.setMessageListener(this::processMessage);
    }

    private void processMessage(Message message) {
        try {
            if (!(message instanceof TextMessage textMessage)) return;

            String json = textMessage.getText();
            String topicName = extractTopicName(message);

            eventStore.save(topicName, json);

        } catch (JMSException e) {
            System.err.println("Error al procesar el mensaje en el Event Store: " + e.getMessage());
        }
    }

    private String extractTopicName(Message message) throws JMSException {
        return message.getJMSDestination().toString().replace("topic://", "");
    }
}