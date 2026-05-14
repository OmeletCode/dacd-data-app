package org.ulpgc.starlink.weather.control;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.ulpgc.starlink.weather.model.WeatherEvent;

public class WeatherController {
    private static final long EXECUTION_INTERVAL_MINUTES = 5;
    private static final String WEATHER_TOPIC = "prediction.Weather";

    private record Location(String name, double lat, double lon) {}

    private final List<Location> TARGET_LOCATIONS = List.of(
            new Location("Las Palmas", 28.12, -15.43),
            new Location("London", 51.5074, -0.1278),
            new Location("Dubai", 25.2048, 55.2708),
            new Location("Singapore", 1.3521, 103.8198),
            new Location("Seattle", 47.6062, -122.3321)
    );

    private final WeatherSupplier supplier;
    private final GsonEventSerializer jsonSerializer;
    private final ActiveMQMessageSender sender;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService workerPool = Executors.newFixedThreadPool(TARGET_LOCATIONS.size());

    public WeatherController(WeatherSupplier supplier) {
        this.supplier = supplier;
        this.jsonSerializer = new GsonEventSerializer();
        this.sender = new ActiveMQMessageSender(WEATHER_TOPIC);
    }

    public void execute() {
        scheduler.scheduleAtFixedRate(this::performExtractionCycle, 0, EXECUTION_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    private void performExtractionCycle() {
        System.out.println("\n[ " + LocalTime.now() + " ] ☁️ Iniciando ciclo de extracción PARALELO...");

        List<CompletableFuture<Void>> futures = TARGET_LOCATIONS.stream()
                .map(loc -> CompletableFuture.runAsync(() -> processLocation(loc), workerPool))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        System.out.println("[ " + LocalTime.now() + " ] ✅ Ciclo de extracción completado.");
    }

    private void processLocation(Location loc) {
        try {
            WeatherEvent weatherEvent = supplier.get(loc.name(), loc.lat(), loc.lon());

            if (weatherEvent != null) {
                String json = jsonSerializer.serialize(weatherEvent);
                sender.sendMessages(List.of(json));
                System.out.println("✅ Clima enviado para: " + loc.name() + " [Thread: " + Thread.currentThread().getName() + "]");
            } else {
                System.out.println("⚠️ Sin datos para: " + loc.name());
            }

        } catch (Exception e) {
            System.err.println("❌ Error al capturar el clima de " + loc.name() + ": " + e.getMessage());
        }
    }
}
