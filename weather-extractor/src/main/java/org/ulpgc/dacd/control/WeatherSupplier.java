package org.ulpgc.dacd.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.Instant;
import org.ulpgc.dacd.model.WeatherEvent;

public class WeatherSupplier {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String SOURCE_SYSTEM = "Weather-Feeder";

    private final String apiKey;
    private final OkHttpClient client;

    public WeatherSupplier(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public WeatherEvent get(double lat, double lon) throws IOException {
        String url = buildUrl(lat, lon);
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                System.err.println("Error de la API de OpenWeather: Código " + response.code());
                return null;
            }
            return parseWeatherEvent(response.body().string(), lat, lon);
        }
    }

    private String buildUrl(double lat, double lon) {
        return String.format("%s?lat=%s&lon=%s&appid=%s&units=metric", BASE_URL, lat, lon, apiKey);
    }

    private WeatherEvent parseWeatherEvent(String jsonBody, double lat, double lon) {
        JsonObject json = JsonParser.parseString(jsonBody).getAsJsonObject();

        long observationTime = json.get("dt").getAsLong();
        JsonObject mainData = json.getAsJsonObject("main");

        double temperature = mainData.get("temp").getAsDouble();
        int humidity = mainData.get("humidity").getAsInt();
        double windSpeed = json.getAsJsonObject("wind").get("speed").getAsDouble();
        String description = json.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString();
        String locationName = json.get("name").getAsString();
        String timestamp = Instant.now().toString();

        return new WeatherEvent(
                timestamp,
                SOURCE_SYSTEM,
                observationTime,
                temperature,
                humidity,
                windSpeed,
                description,
                locationName,
                lat,
                lon
        );
    }
}