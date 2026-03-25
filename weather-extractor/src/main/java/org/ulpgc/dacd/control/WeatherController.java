package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Weather;
import java.util.Timer;
import java.util.TimerTask;

public class WeatherController {
    private final WeatherSupplier supplier;
    private final WeatherSerializer serializer;

    public WeatherController(WeatherSupplier supplier, WeatherSerializer serializer) {
        this.supplier = supplier;
        this.serializer = serializer;
    }

    public void execute() {
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Obteniendo datos meteorológicos...");
                try {
                    // Usamos tu método real con las coordenadas de Las Palmas
                    Weather weather = supplier.get(28.12, -15.43);

                    if (weather != null) {
                        // Ahora le pasamos el objeto COMPLETO a la base de datos, no pieza a pieza
                        serializer.saveWeather(weather);
                        System.out.println("Clima de " + weather.location() + " guardado en la DB común.");
                    }
                } catch (Exception e) {
                    System.err.println("Error al capturar el clima: " + e.getMessage());
                }
            }
        }, 0, 15 * 60 * 1000);
    }
}