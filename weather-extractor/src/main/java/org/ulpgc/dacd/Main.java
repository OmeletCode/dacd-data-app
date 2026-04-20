package org.ulpgc.dacd;

import org.ulpgc.dacd.control.WeatherController;
import org.ulpgc.dacd.control.WeatherSupplier;

public class Main {
    public static void main(String[] args) {
        // 1. Leemos la clave de forma segura desde las variables de entorno
        String key = System.getenv("OPENWEATHER_API_KEY");

        // Comprobación de seguridad por si se nos olvida configurarla
        if (key == null || key.isEmpty()) {
            System.err.println("ERROR: No se ha encontrado la API Key.");
            System.err.println("Por favor, configura la variable de entorno 'OPENWEATHER_API_KEY' en tu sistema o IDE.");
            return; // Detenemos la ejecución para no hacer peticiones inválidas
        }

        // 2. Instanciamos el extractor de la API con la clave segura
        WeatherSupplier supplier = new WeatherSupplier(key);

        // 3. Ensamblamos el controlador pasándole el supplier
        WeatherController controller = new WeatherController(supplier);

        // 4. ¡Arrancamos el motor!
        System.out.println("--- Iniciando recolector de clima (Seguro y JSON Ready) ---");
        controller.execute();
    }
}