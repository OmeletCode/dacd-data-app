package org.ulpgc.dacd.model;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.List;

public record ActiveMQMessageSender(String topicName) {
    private static final String BROKER_URL = "failover:(tcp://localhost:61616)";

    public void sendMessages(List<String> jsonEvents) {
        if (jsonEvents == null || jsonEvents.isEmpty()) return;

        try {
            Connection connection = createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createTopic(topicName));

            publishEvents(session, producer, jsonEvents);

            connection.close();
        } catch (JMSException e) {
            System.err.println("Error al enviar mensajes a ActiveMQ en el topic " + topicName + ": " + e.getMessage());
        }
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }

    private void publishEvents(Session session, MessageProducer producer, List<String> jsonEvents) throws JMSException {
        for (String json : jsonEvents) {
            producer.send(session.createTextMessage(json));
        }
        System.out.println("-> " + jsonEvents.size() + " mensajes enviados con éxito al topic: " + topicName);
    }
}