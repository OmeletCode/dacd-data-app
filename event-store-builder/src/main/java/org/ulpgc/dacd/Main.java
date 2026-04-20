package org.ulpgc.dacd;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Event Store Builder ---");

        ActiveMQSubscriber subscriber = new ActiveMQSubscriber();
        subscriber.start();
    }
}