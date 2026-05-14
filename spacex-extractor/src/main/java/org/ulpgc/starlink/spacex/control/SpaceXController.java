package org.ulpgc.starlink.spacex.control;

import org.ulpgc.starlink.spacex.model.SatelliteEvent;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpaceXController {
    private static final long EXECUTION_INTERVAL_SECONDS = 60;
    private static final String SPACEX_TOPIC = "sensor.SpaceX";

    private final SpaceXSupplier supplier;
    private final GsonEventSerializer jsonSerializer;
    private final ActiveMQMessageSender sender;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SpaceXController(SpaceXSupplier supplier) {
        this.supplier = supplier;
        this.jsonSerializer = new GsonEventSerializer();
        this.sender = new ActiveMQMessageSender(SPACEX_TOPIC);
    }

    public void execute() {
        scheduler.scheduleAtFixedRate(this::performExtractionCycle, 0, EXECUTION_INTERVAL_SECONDS, TimeUnit.SECONDS);
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
            System.out.println("⏳ Esperando " + EXECUTION_INTERVAL_SECONDS + " segundos para el próximo ciclo...");

        } catch (Exception e) {
            System.err.println("❌ ERROR en el ciclo de captura: " + e.getMessage());
        }
    }
}
