package org.ulpgc.dacd;

import org.ulpgc.dacd.control.WeatherController;
import org.ulpgc.dacd.control.WeatherSupplier;

public class Main {
    public static void main(String[] args) {
        // 1. Instanciamos el extractor de la API
        String key = "abe03c99d7dcf2d52fa36ac4b4622a66";
        WeatherSupplier supplier = new WeatherSupplier(key);

        // Ya no usamos la base de datos, así que borramos esa parte

        // 2. Ensamblamos el controlador pasándole solo el supplier
        WeatherController controller = new WeatherController(supplier);

        // 3. ¡Arrancamos el motor!
        System.out.println("--- Iniciando recolector de clima (JSON Ready) ---");
        controller.execute();
    }
}