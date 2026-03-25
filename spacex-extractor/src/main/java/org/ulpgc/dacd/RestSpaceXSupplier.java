package org.ulpgc.dacd;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RestSpaceXSupplier implements SpaceXSupplier {
    private final OkHttpClient client = new OkHttpClient();
    private final String url = "https://api.spacexdata.com/v4/starlink";

    @Override
    public List<Satellite> getSatellites() {
        List<Satellite> satellites = new ArrayList<>();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String rawJson = response.body().string();
                JsonArray starlinks = JsonParser.parseString(rawJson).getAsJsonArray();

                for (int i = 0; i < starlinks.size(); i++) {
                    JsonObject obj = starlinks.get(i).getAsJsonObject();

                    String name = "Desconocido";
                    if (obj.has("spaceTrack") && !obj.get("spaceTrack").isJsonNull()) {
                        name = obj.get("spaceTrack").getAsJsonObject().get("OBJECT_NAME").getAsString();
                    } else if (obj.has("id")) {
                        name = obj.get("id").getAsString();
                    }

                    double lat = obj.has("latitude") && !obj.get("latitude").isJsonNull() ? obj.get("latitude").getAsDouble() : 0.0;
                    double lon = obj.has("longitude") && !obj.get("longitude").isJsonNull() ? obj.get("longitude").getAsDouble() : 0.0;
                    double vel = obj.has("velocity_kms") && !obj.get("velocity_kms").isJsonNull() ? obj.get("velocity_kms").getAsDouble() : 0.0;

                    satellites.add(new Satellite(name, lat, lon, vel, obj.toString()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error obteniendo datos de SpaceX: " + e.getMessage());
        }
        return satellites;
    }
}