package org.ulpgc.dacd;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class ActiveMQSubscriber {
    private static final String URL = "tcp://localhost:61616";
    // El ID es obligatorio para que la suscripción sea "Duradera"
    private static final String CLIENT_ID = "EventStoreBuilder-Node1";

    public void start() {
        // Instanciamos nuestro nuevo archivero
        FileEventStore eventStore = new FileEventStore();

        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
            Connection connection = connectionFactory.createConnection();
            connection.setClientID(CLIENT_ID);
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic spaceXTopic = session.createTopic("sensor.SpaceX");
            MessageConsumer spaceXConsumer = session.createDurableSubscriber(spaceXTopic, "SpaceX-Sub");

            Topic weatherTopic = session.createTopic("prediction.Weather");
            MessageConsumer weatherConsumer = session.createDurableSubscriber(weatherTopic, "Weather-Sub");

            // --- LA MAGIA OCURRE AQUÍ ---
            MessageListener listener = new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        if (message instanceof TextMessage textMessage) {
                            String json = textMessage.getText();
                            // Averiguamos de qué canal viene el mensaje
                            String topicName = message.getJMSDestination().toString().replace("topic://", "");

                            // Le decimos al archivero que haga su trabajo
                            eventStore.save(topicName, json);
                        }
                    } catch (JMSException e) {
                        System.err.println("Error al leer el mensaje: " + e.getMessage());
                    }
                }
            };

            spaceXConsumer.setMessageListener(listener);
            weatherConsumer.setMessageListener(listener);

            System.out.println("✅ Event Store Builder conectado y esperando mensajes...");

        } catch (JMSException e) {
            System.err.println("Error conectando a ActiveMQ: " + e.getMessage());
        }
    }
}