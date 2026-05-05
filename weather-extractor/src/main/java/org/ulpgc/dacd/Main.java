package org.ulpgc.dacd;

import org.ulpgc.dacd.control.WeatherController;
import org.ulpgc.dacd.control.WeatherSupplier;

public class Main {
    private static final String API_KEY_ENV_VAR = "OPENWEATHER_API_KEY";

    public static void main(String[] args) {
        System.out.println("--- Iniciando el Extractor Meteorológico ---");

        String apiKey = getApiKeyOrExit();

        WeatherSupplier supplier = new WeatherSupplier(apiKey);
        WeatherController controller = new WeatherController(supplier);

        controller.execute();
    }

    private static String getApiKeyOrExit() {
        String key = System.getenv(API_KEY_ENV_VAR);

        if (key == null || key.isBlank()) {
            System.err.println("❌ ERROR: Variable de entorno '" + API_KEY_ENV_VAR + "' no configurada.");
            System.err.println("Por favor, configúrala en tu sistema o IDE antes de arrancar el recolector.");
            System.exit(1);
        }

        return key;
    }
}