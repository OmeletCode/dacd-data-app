package org.ulpgc.dacd.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ulpgc.dacd.model.Weather;
import java.io.IOException;

public class WeatherSupplier {
    private String apiKey;
    private OkHttpClient client = new OkHttpClient();

    public WeatherSupplier(String apiKey) {
        this.apiKey = apiKey;
    }

    public Weather get(double lat, double lon) throws IOException {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric";

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            long dt = json.get("dt").getAsLong();
            double t = json.get("main").getAsJsonObject().get("temp").getAsDouble();
            int h = json.get("main").getAsJsonObject().get("humidity").getAsInt();
            double w = json.get("wind").getAsJsonObject().get("speed").getAsDouble();
            String d = json.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("description").getAsString();
            String n = json.get("name").getAsString();

            return new Weather(dt, t, h, w, d, n);
        }
    }
}
