package org.ulpgc.dacd;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

                // Aquí usamos GSON para convertir el texto en una lista (Array) de objetos
                JsonArray starlinks = JsonParser.parseString(jsonResponse).getAsJsonArray();

                System.out.println("¡Conexión exitosa!");
                System.out.println("Total de satélites detectados: " + starlinks.size());
                System.out.println("\nListado de los primeros 10 satélites:");
                System.out.println("--------------------------------------");

                for (int i = 0; i < Math.min(starlinks.size(), 10); i++) {
                    JsonElement element = starlinks.get(i);
                    // Extraemos el nombre que está dentro de "spaceTrack" -> "OBJECT_NAME"
                    String name = element.getAsJsonObject()
                            .get("spaceTrack").getAsJsonObject()
                            .get("OBJECT_NAME").getAsString();

                    System.out.println((i + 1) + ". " + name);
                }

            } else {
                System.out.println("Error en la petición. Código: " + response.code());
            }
        } catch (IOException e) {
            System.out.println("Error de red: " + e.getMessage());
        }
    }
}