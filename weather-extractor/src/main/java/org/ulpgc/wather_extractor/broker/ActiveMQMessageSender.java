package org.ulpgc.wather_extractor.broker;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;

public record ActiveMQMessageSender(String topicName) {

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
        // 🚀 MAGIA DE DOCKER: Leemos la variable de entorno
        String brokerUrl = System.getenv("ACTIVEMQ_URL");
        if (brokerUrl == null || brokerUrl.isEmpty()) {
            brokerUrl = "failover:(tcp://localhost:61616)"; // Fallback para IntelliJ
        }

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
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