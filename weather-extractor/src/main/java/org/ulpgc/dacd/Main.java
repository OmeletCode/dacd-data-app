package org.ulpgc.dacd;

import org.ulpgc.dacd.control.WeatherSupplier;
import org.ulpgc.dacd.model.Weather;
import org.ulpgc.dacd.DatabaseManager;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        String key = "abe03c99d7dcf2d52fa36ac4b4622a66";
        WeatherSupplier supplier = new WeatherSupplier(key);

        DatabaseManager dbManager = new DatabaseManager();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    // 1. Obtenemos el objeto Weather con los datos de la API
                    Weather data = supplier.get(28.12, -15.43);

                    // 2. Le pasamos las piezas sueltas a la base de datos (Desacoplamiento)
                    dbManager.saveWeather(
                            data.timestamp(),
                            data.temp(),
                            data.humidity(),
                            data.windSpeed(),
                            data.description(),
                            data.location()
                    );

                    System.out.println("Clima de " + data.location() + " guardado en la DB común.");
                } catch (Exception e) {
                    System.err.println("Error al capturar el clima: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 0, 15 * 60 * 1000); // Ejecuta ahora y luego cada 15 minutos
    }
}