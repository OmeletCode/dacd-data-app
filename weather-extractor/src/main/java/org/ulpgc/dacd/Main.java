package org.ulpgc.dacd;

import org.ulpgc.dacd.control.WeatherController;
import org.ulpgc.dacd.control.WeatherSupplier;
import org.ulpgc.dacd.control.SqliteWeatherSerializer;
import org.ulpgc.dacd.control.WeatherSerializer;

public class Main {
    public static void main(String[] args) {
        // 1. Instanciamos el extractor de la API
        String key = "abe03c99d7dcf2d52fa36ac4b4622a66";
        WeatherSupplier supplier = new WeatherSupplier(key);

        // 2. Instanciamos el guardado en base de datos
        String dbUrl = "jdbc:sqlite:spacex_data.db";
        WeatherSerializer serializer = new SqliteWeatherSerializer(dbUrl);

        // 3. Ensamblamos el controlador pasándole las dos piezas
        WeatherController controller = new WeatherController(supplier, serializer);

        // 4. ¡Arrancamos el motor!
        System.out.println("Iniciando recolector de clima...");
        controller.execute();
    }
}