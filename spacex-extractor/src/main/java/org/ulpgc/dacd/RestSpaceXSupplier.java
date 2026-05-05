package org.ulpgc.dacd;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ulpgc.dacd.model.SatelliteEvent;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestSpaceXSupplier implements SpaceXSupplier {
    private static final String API_URL = "https://api.spacexdata.com/v4/starlink";
    private static final String SOURCE_SYSTEM = "SpaceX-Feeder";
    private static final String DEFAULT_NAME = "Desconocido";

    private final OkHttpClient client;

    public RestSpaceXSupplier() {
        this.client = new OkHttpClient();
    }

    @Override
    public List<SatelliteEvent> getSatellites() {
        Request request = new Request.Builder().url(API_URL).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return parseSatellites(response.body().string());
            }
        } catch (IOException e) {
            System.err.println("Error obteniendo datos de SpaceX: " + e.getMessage());
        }

        return Collections.emptyList();
    }

    private List<SatelliteEvent> parseSatellites(String jsonBody) {
        List<SatelliteEvent> events = new ArrayList<>();
        JsonArray starlinks = JsonParser.parseString(jsonBody).getAsJsonArray();
        String currentTimestamp = Instant.now().toString();

        for (int i = 0; i < starlinks.size(); i++) {
            JsonObject obj = starlinks.get(i).getAsJsonObject();
            events.add(buildSatelliteEvent(obj, currentTimestamp));
        }

        return events;
    }

    private SatelliteEvent buildSatelliteEvent(JsonObject obj, String timestamp) {
        String name = extractName(obj);
        double latitude = extractCoordinate(obj, "latitude");
        double longitude = extractCoordinate(obj, "longitude");

        return new SatelliteEvent(timestamp, SOURCE_SYSTEM, name, latitude, longitude);
    }

    private String extractName(JsonObject obj) {
        if (obj.has("spaceTrack") && !obj.get("spaceTrack").isJsonNull()) {
            return obj.get("spaceTrack").getAsJsonObject().get("OBJECT_NAME").getAsString();
        }
        if (obj.has("id") && !obj.get("id").isJsonNull()) {
            return obj.get("id").getAsString();
        }
        return DEFAULT_NAME;
    }

    private double extractCoordinate(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsDouble();
        }
        return 0.0;
    }
}