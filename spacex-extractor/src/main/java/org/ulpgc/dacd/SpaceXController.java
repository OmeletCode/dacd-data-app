package org.ulpgc.dacd;

import org.ulpgc.dacd.model.ActiveMQMessageSender;
import org.ulpgc.dacd.model.GsonEventSerializer;
import org.ulpgc.dacd.model.SatelliteEvent;

import java.time.LocalTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SpaceXController {
    private static final long EXECUTION_INTERVAL_MS = 60000;
    private static final String SPACEX_TOPIC = "sensor.SpaceX";

    private final SpaceXSupplier supplier;
    private final GsonEventSerializer jsonSerializer;
    private final ActiveMQMessageSender sender;

    public SpaceXController(SpaceXSupplier supplier) {
        this.supplier = supplier;
        this.jsonSerializer = new GsonEventSerializer();
        this.sender = new ActiveMQMessageSender(SPACEX_TOPIC);
    }

    public void execute() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                performExtractionCycle();
            }
        }, 0, EXECUTION_INTERVAL_MS);
    }

    private void performExtractionCycle() {
        System.out.println("\n[ " + LocalTime.now() + " ] 🛰️ Iniciando captura de SpaceX...");

        try {
            List<SatelliteEvent> events = supplier.getSatellites();

            if (events == null || events.isEmpty()) {
                System.out.println("⚠️ ATENCIÓN: La lista de satélites llegó vacía en este ciclo.");
                return;
            }

            List<String> jsonEvents = events.stream()
                    .map(jsonSerializer::serialize)
                    .toList();

            sender.sendMessages(jsonEvents);

            System.out.println("✅ ÉXITO: Se han procesado y enviado " + events.size() + " satélites.");
            System.out.println("⏳ Esperando " + (EXECUTION_INTERVAL_MS / 1000) + " segundos para el próximo ciclo...");

        } catch (Exception e) {
            System.err.println("❌ ERROR en el ciclo de captura: " + e.getMessage());
        }
    }
}