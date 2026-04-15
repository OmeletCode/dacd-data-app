package org.ulpgc.dacd;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class ActiveMQSubscriber {
    private static final String URL = "tcp://localhost:61616";
    // El ID es obligatorio para que la suscripción sea "Duradera"
    private static final String CLIENT_ID = "EventStoreBuilder-Node1";

    public void start() {
        try {
            // 1. Conectar al Broker
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
            Connection connection = connectionFactory.createConnection();
            connection.setClientID(CLIENT_ID); // ¡Clave para cumplir el requisito!
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 2. Suscribirnos a los dos canales
            Topic spaceXTopic = session.createTopic("sensor.SpaceX");
            MessageConsumer spaceXConsumer = session.createDurableSubscriber(spaceXTopic, "SpaceX-Sub");

            Topic weatherTopic = session.createTopic("prediction.Weather");
            MessageConsumer weatherConsumer = session.createDurableSubscriber(weatherTopic, "Weather-Sub");

            // 3. Crear la acción: ¿Qué hacemos cuando llega un mensaje?
            MessageListener listener = new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        if (message instanceof TextMessage textMessage) {
                            String json = textMessage.getText();
                            System.out.println("📥 Nuevo evento recibido: " + json);
                            // TODO: Aquí será donde guardaremos el JSON en las carpetas
                        }
                    } catch (JMSException e) {
                        System.err.println("Error al leer el mensaje: " + e.getMessage());
                    }
                }
            };

            // 4. Asignamos la acción a nuestros canales
            spaceXConsumer.setMessageListener(listener);
            weatherConsumer.setMessageListener(listener);

            System.out.println("✅ Event Store Builder conectado y escuchando...");

        } catch (JMSException e) {
            System.err.println("Error conectando a ActiveMQ: " + e.getMessage());
        }
    }
}