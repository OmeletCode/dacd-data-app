package org.ulpgc.dacd.control;

import java.time.LocalTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.model.ActiveMQMessageSender;
import org.ulpgc.dacd.model.GsonEventSerializer;

public class WeatherController {
    private static final long EXECUTION_INTERVAL_MS = 15 * 60 * 1000;
    private static final String WEATHER_TOPIC = "prediction.Weather";

    // Coordenadas de Las Palmas de Gran Canaria
    private static final double TARGET_LATITUDE = 28.12;
    private static final double TARGET_LONGITUDE = -15.43;

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
        System.out.println("\n[ " + LocalTime.now() + " ] ☁️ Obteniendo datos meteorológicos...");

        try {
            WeatherEvent weatherEvent = supplier.get(TARGET_LATITUDE, TARGET_LONGITUDE);

            if (weatherEvent == null) {
                System.out.println("⚠️ ATENCIÓN: No se pudo obtener el clima en este ciclo.");
                return; // Early Return
            }

            String json = jsonSerializer.serialize(weatherEvent);
            sender.sendMessages(List.of(json));

        } catch (Exception e) {
            System.err.println("❌ Error al capturar el clima: " + e.getMessage());
        }
    }
}