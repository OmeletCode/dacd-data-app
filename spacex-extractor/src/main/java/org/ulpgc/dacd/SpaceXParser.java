package org.ulpgc.dacd;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;

public class SpaceXParser {
    public List<Satellite> parse(String rawJson) {
        List<Satellite> satellites = new ArrayList<>();
        JsonArray starlinks = JsonParser.parseString(rawJson).getAsJsonArray();

        for (int i = 0; i < starlinks.size(); i++) {
            JsonObject obj = starlinks.get(i).getAsJsonObject();

            // Extraemos el nombre
            String name = "Desconocido";
            if (obj.has("spaceTrack") && !obj.get("spaceTrack").isJsonNull()) {
                name = obj.get("spaceTrack").getAsJsonObject().get("OBJECT_NAME").getAsString();
            } else if (obj.has("id")) {
                name = obj.get("id").getAsString();
            }

            // Extraemos la telemetría (latitud, longitud y velocidad)
            double lat = obj.has("latitude") && !obj.get("latitude").isJsonNull() ? obj.get("latitude").getAsDouble() : 0.0;
            double lon = obj.has("longitude") && !obj.get("longitude").isJsonNull() ? obj.get("longitude").getAsDouble() : 0.0;
            double vel = obj.has("velocity_kms") && !obj.get("velocity_kms").isJsonNull() ? obj.get("velocity_kms").getAsDouble() : 0.0;

            // Creamos el objeto con todos los datos nuevos
            satellites.add(new Satellite(name, lat, lon, vel, obj.toString()));
        }
        return satellites;
    }
}