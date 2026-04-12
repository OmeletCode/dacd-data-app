package org.ulpgc.dacd; // Cambia esto según tu paquete

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class ActiveMQMessageSender {
    // La dirección de tu Broker (la misma que comprobamos en el Punto 1)
    private static final String URL = "tcp://localhost:61616";
    private final String topicName;

    // Al construir el cartero, le decimos a qué "Canal" (Topic) debe enviar el mensaje
    public ActiveMQMessageSender(String topicName) {
        this.topicName = topicName;
    }

    public void sendMessage(String jsonEvent) {
        try {
            // 1. Conectamos con ActiveMQ
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // 2. Creamos una sesión
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 3. Buscamos el buzón/canal (Topic)
            Destination destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);

            // 4. Metemos nuestro JSON en un mensaje de texto y lo enviamos
            TextMessage message = session.createTextMessage(jsonEvent);
            producer.send(message);

            System.out.println("-> Mensaje enviado con éxito al topic: " + topicName);

            // 5. Cerramos la conexión para no dejar puertas abiertas
            connection.close();

        } catch (JMSException e) {
            System.err.println("Error al enviar el mensaje a ActiveMQ: " + e.getMessage());
        }
    }
}