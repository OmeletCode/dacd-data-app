package org.ulpgc.dacd.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.time.Instant; // --- NUEVO: Importante para el Timestamp ---

public class WeatherSupplier {
    private String apiKey;
    private OkHttpClient client = new OkHttpClient();

    public WeatherSupplier(String apiKey) {
        this.apiKey = apiKey;
    }

    // --- NUEVO: Ahora devolvemos un WeatherEvent en lugar de Weather ---
    public WeatherEvent get(double lat, double lon) throws IOException {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric";

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            // 1. Tu código antiguo que saca los datos
            long dt = json.get("dt").getAsLong();
            double t = json.get("main").getAsJsonObject().get("temp").getAsDouble();
            int h = json.get("main").getAsJsonObject().get("humidity").getAsInt();
            double w = json.get("wind").getAsJsonObject().get("speed").getAsDouble();
            String d = json.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("description").getAsString();
            String n = json.get("name").getAsString();

            // --- NUEVO: APLICANDO EL PUNTO 5 ---
            // 2. Generamos los campos obligatorios del evento
            String ts = Instant.now().toString(); // Timestamp en formato UTC
            String ss = "Weather-Feeder";         // El origen del dato

            // 3. Creamos el Evento con todos los datos y lo devolvemos
            return new WeatherEvent(ts, ss, dt, t, h, w, d, n, lat, lon);
        }
    }
}