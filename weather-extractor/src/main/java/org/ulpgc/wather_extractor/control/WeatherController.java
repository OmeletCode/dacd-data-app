package org.ulpgc.wather_extractor.control;

import java.time.LocalTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.ulpgc.wather_extractor.model.WeatherEvent;
import org.ulpgc.wather_extractor.broker.ActiveMQMessageSender;
import org.ulpgc.wather_extractor.broker.GsonEventSerializer;

public class WeatherController {
    private static final long EXECUTION_INTERVAL_MS = 15 * 60 * 1000;
    private static final String WEATHER_TOPIC = "prediction.Weather";

    // Creamos un pequeño Record interno para manejar las ciudades cómodamente
    private record Location(String name, double lat, double lon) {}

    // Lista de ubicaciones reales por el mundo para probar distintos climas
    private final List<Location> TARGET_LOCATIONS = List.of(
            new Location("Las Palmas", 28.12, -15.43),
            new Location("London", 51.5074, -0.1278),    // Posible lluvia
            new Location("Dubai", 25.2048, 55.2708),     // Seguramente despejado
            new Location("Singapore", 1.3521, 103.8198), // Posibles tormentas
            new Location("Seattle", 47.6062, -122.3321)  // Probabilidad de nubes/lluvia
    );

    private final WeatherSupplier supplier;
    private final GsonEventSerializer jsonSerializer;
    private final ActiveMQMessageSender sender;

    public WeatherController(WeatherSupplier supplier) {
        this.supplier = supplier;
        this.jsonSerializer = new GsonEventSerializer();
        this.sender = new ActiveMQMessageSender(WEATHER_TOPIC);
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
        System.out.println("\n[ " + LocalTime.now() + " ] ☁️ Obteniendo datos meteorológicos globales...");

        // Iteramos por nuestra lista de ciudades y mandamos un mensaje por cada una
        for (Location loc : TARGET_LOCATIONS) {
            try {
                // Busca esta línea dentro de tu método performExtractionCycle():
                WeatherEvent weatherEvent = supplier.get(loc.name(), loc.lat(), loc.lon());

                if (weatherEvent != null) {
                    String json = jsonSerializer.serialize(weatherEvent);
                    sender.sendMessages(List.of(json));
                    System.out.println("✅ Clima enviado para: " + loc.name());
                } else {
                    System.out.println("⚠️ Sin datos para: " + loc.name());
                }

            } catch (Exception e) {
                System.err.println("❌ Error al capturar el clima de " + loc.name() + ": " + e.getMessage());
            }
        }
    }
}