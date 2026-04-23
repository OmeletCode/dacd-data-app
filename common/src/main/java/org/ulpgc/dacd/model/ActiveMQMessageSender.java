package org.ulpgc.dacd.model;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.List;

public class ActiveMQMessageSender {
    private static final String URL = "failover:(tcp://localhost:61616)";
    private final String topicName;

    public ActiveMQMessageSender(String topicName) {
        this.topicName = topicName;
    }

    // AHORA RECIBE UNA LISTA DE JSONs
    public void sendMessages(List<String> jsonEvents) {
        try {
            // 1. Abrimos la puerta UNA SOLA VEZ
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
            Connection connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);

            // 2. Enviamos todos los mensajes por la misma conexión
            for (String json : jsonEvents) {
                TextMessage message = session.createTextMessage(json);
                producer.send(message);
            }

            System.out.println("-> " + jsonEvents.size() + " mensajes enviados con éxito al topic: " + topicName);

            // 3. Cerramos la puerta
            connection.close();

        } catch (JMSException e) {
            System.err.println("Error al enviar mensajes a ActiveMQ: " + e.getMessage());
        }
    }
}