package org.ulpgc.dacd;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando el Extractor Automático de Starlink ---");
        System.out.println("El programa se quedará en ejecución guardando datos periódicamente.\n");

        Timer timer = new Timer();

        // Creamos la tarea que se va a repetir
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                extraerYGuardarDatos();
            }
        };

        // Configuramos el temporizador:
        // 0 = empezar ahora mismo.
        // 60000 = repetirlo cada 60.000 milisegundos (1 minuto).
        timer.scheduleAtFixedRate(task, 0, 3600000);
    }

    // Aquí metemos todo el código que ya tenías funcionando perfectamente
    private static void extraerYGuardarDatos() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.spacexdata.com/v4/starlink";
        Request request = new Request.Builder().url(url).build();

        System.out.println("[ " + java.time.LocalTime.now() + " ] Iniciando nueva captura de datos...");

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                JsonArray starlinks = JsonParser.parseString(jsonResponse).getAsJsonArray();

                DatabaseManager dbManager = new DatabaseManager();
                System.out.println("Guardando " + starlinks.size() + " satélites en SQLite...");

                for (int i = 0; i < starlinks.size(); i++) {
                    JsonObject obj = starlinks.get(i).getAsJsonObject();
                    String name = "Desconocido";
                    if (obj.has("spaceTrack") && !obj.get("spaceTrack").isJsonNull()) {
                        name = obj.get("spaceTrack").getAsJsonObject().get("OBJECT_NAME").getAsString();
                    } else if (obj.has("id")) {
                        name = obj.get("id").getAsString();
                    }
                    dbManager.saveSatellite(name, obj.toString());
                }

                System.out.println("¡Captura completada! Esperando al siguiente ciclo...\n");

            } else {
                System.out.println("Error en la petición. Código: " + response.code());
            }
        } catch (IOException e) {
            System.out.println("Error de red: " + e.getMessage());
        }
    }
}