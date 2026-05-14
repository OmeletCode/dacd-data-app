package org.ulpgc.starlink.eventstore;

import org.ulpgc.starlink.eventstore.control.ActiveMQSubscriber;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Starting Event Store Builder ---");

        ActiveMQSubscriber subscriber = new ActiveMQSubscriber();
        subscriber.start();
    }
}
