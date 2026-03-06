package org.ulpgc.dacd;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.spacexdata.com/v4/starlink";

        Request request = new Request.Builder()
                .url(url)
                .build();

        System.out.println("--- Extractor de Satélites Starlink ---");
        System.out.println("Conectando con la API de SpaceX...");

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                JsonArray starlinks = JsonParser.parseString(jsonResponse).getAsJsonArray();

                System.out.println("¡Conexión exitosa!");
                System.out.println("Total de satélites detectados: " + starlinks.size());

                DatabaseManager dbManager = new DatabaseManager();
                System.out.println("Guardando " + starlinks.size() + " satélites en SQLite. Esto puede tardar unos segundos...");

                // Iteramos sobre TODOS los satélites para guardarlos
                for (int i = 0; i < starlinks.size(); i++) {
                    JsonObject obj = starlinks.get(i).getAsJsonObject();

                    // Extraemos el nombre con cuidado por si algún dato viene vacío
                    String name = "Desconocido";
                    if (obj.has("spaceTrack") && !obj.get("spaceTrack").isJsonNull()) {
                        name = obj.get("spaceTrack").getAsJsonObject().get("OBJECT_NAME").getAsString();
                    } else if (obj.has("id")) {
                        name = obj.get("id").getAsString(); // Plan B
                    }

                    // Guardamos en la base de datos (Nombre y el JSON completo en bruto)
                    dbManager.saveSatellite(name, obj.toString());
                }

                System.out.println("¡Proceso de guardado completado con éxito! Revisa la carpeta del proyecto.");

            } else {
                System.out.println("Error en la petición. Código: " + response.code());
            }
        } catch (IOException e) {
            System.out.println("Error de red: " + e.getMessage());
        }
    }
}