package org.ulpgc.dacd;

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

        System.out.println("Conectando con SpaceX...");

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                System.out.println("¡Conexión exitosa! Código: " + response.code());
                String jsonResponse = response.body().string();
                System.out.println("Primeros datos recibidos:\n" +
                        jsonResponse.substring(0, Math.min(jsonResponse.length(), 500)) + "...\n[CONTINÚA]");
            } else {
                System.out.println("Error al conectar. Código HTTP: " + response.code());
            }
        } catch (IOException e) {
            System.out.println("Hubo un fallo en la red: " + e.getMessage());
        }
    }
}