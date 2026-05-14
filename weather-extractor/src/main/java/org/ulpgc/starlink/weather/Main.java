package org.ulpgc.starlink.weather;

import org.ulpgc.starlink.weather.control.WeatherController;
import org.ulpgc.starlink.weather.control.WeatherSupplier;

public class Main {
    private static final String API_KEY_ENV_VAR = "OPENWEATHER_API_KEY";

    public static void main(String[] args) {
        System.out.println("--- Starting Automatic Weather Extractor ---");

        String apiKey = getApiKeyOrExit();

        WeatherSupplier supplier = new WeatherSupplier(apiKey);
        WeatherController controller = new WeatherController(supplier);

        controller.execute();
    }

    private static String getApiKeyOrExit() {
        String key = System.getenv(API_KEY_ENV_VAR);

        if (key == null || key.isBlank()) {
            System.err.println("❌ ERROR: Environment variable '" + API_KEY_ENV_VAR + "' not set.");
            System.err.println("Please set it in your system or IDE before running the extractor.");
            System.exit(1);
        }

        return key;
    }
}
