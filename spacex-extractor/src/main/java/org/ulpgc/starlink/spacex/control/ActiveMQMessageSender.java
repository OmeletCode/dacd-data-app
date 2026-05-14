package org.ulpgc.starlink.spacex.control;

import org.apache.activemq.ActiveMQConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.List;

public record ActiveMQMessageSender(String topicName) {
    private static final Logger logger = LoggerFactory.getLogger(ActiveMQMessageSender.class);


    public void sendMessages(List<String> jsonEvents) {
        if (jsonEvents == null || jsonEvents.isEmpty()) return;

        Connection connection = null;
        Session session = null;
        try {
            connection = createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            MessageProducer producer = session.createProducer(session.createTopic(topicName));
            publishEvents(session, producer, jsonEvents);
            
        } catch (JMSException e) {
            logger.error("Error al enviar mensajes a ActiveMQ en el topic {}: {}", topicName, e.getMessage());
        } finally {
            try {
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                logger.error("Error al cerrar la conexión ActiveMQ: {}", e.getMessage());
            }
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
        logger.info("-> {} mensajes enviados con éxito al topic: {}", jsonEvents.size(), topicName);
    }
}