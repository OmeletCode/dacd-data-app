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
        System.out.println("\n[ " + LocalTime.now() + " ] 🛰️ Starting SpaceX telemetry capture...");

        try {
            List<SatelliteEvent> events = supplier.getSatellites();

            if (events == null || events.isEmpty()) {
                System.out.println("⚠️ WARNING: Satellite list received empty in this cycle.");
                return;
            }

            List<String> jsonEvents = events.stream()
                    .map(jsonSerializer::serialize)
                    .toList();

            sender.sendMessages(jsonEvents);

            System.out.println("✅ SUCCESS: Processed and sent " + events.size() + " satellites.");
            System.out.println("⏳ Waiting " + EXECUTION_INTERVAL_SECONDS + " seconds for next cycle...");

        } catch (Exception e) {
            System.err.println("❌ ERROR in extraction cycle: " + e.getMessage());
        }
    }
}
