package org.ulpgc.dacd;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SpaceXController {
    private final SpaceXSupplier supplier;

    public SpaceXController(SpaceXSupplier supplier) {
        this.supplier = supplier;
    }

    public void execute() {
        Timer timer = new Timer();
        GsonEventSerializer jsonSerializer = new GsonEventSerializer();
        ActiveMQMessageSender sender = new ActiveMQMessageSender("sensor.SpaceX");

        // Definimos el intervalo: 60.000 ms = 1 minuto
        // Esto hará que el mapa se actualice con frecuencia suficiente para la demo
        long interval = 60000;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("\n[ " + java.time.LocalTime.now() + " ] 🛰️ Iniciando captura de SpaceX...");

                try {
                    List<SpaceXEvent> eventos = supplier.getSatellites();

                    if (eventos != null && !eventos.isEmpty()) {
                        List<String> jsonEvents = new ArrayList<>();

                        // Serialización a JSON
                        for (SpaceXEvent evento : eventos) {
                            jsonEvents.add(jsonSerializer.serialize(evento));
                        }

                        // Envío al Broker ActiveMQ
                        sender.sendMessages(jsonEvents);

                        System.out.println("✅ ÉXITO: Se han procesado y enviado " + eventos.size() + " satélites.");
                        System.out.println("⏳ Esperando 60 segundos para el próximo ciclo...");

                    } else {
                        System.out.println("⚠️ ATENCIÓN: La lista de satélites llegó vacía en este ciclo.");
                    }
                } catch (Exception e) {
                    System.err.println("❌ ERROR en el ciclo de captura: " + e.getMessage());
                }
            }
        }, 0, interval);
    }
}