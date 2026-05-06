package org.ulpgc.event_store_builder;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Event Store Builder (Data Lake) ---");

        FileEventStore eventStore = new FileEventStore();
        ActiveMQSubscriber subscriber = new ActiveMQSubscriber(eventStore);

        subscriber.start();
    }
}