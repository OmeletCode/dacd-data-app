package org.ulpgc.starlink.eventstore;

import org.ulpgc.starlink.eventstore.control.ActiveMQSubscriber;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Event Store Builder ---");

        ActiveMQSubscriber subscriber = new ActiveMQSubscriber();
        subscriber.start();
    }
}
