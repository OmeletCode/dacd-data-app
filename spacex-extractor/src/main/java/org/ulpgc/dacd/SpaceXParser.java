package org.ulpgc.dacd;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;

public class SpaceXParser {
    // Recibe el texto de internet y lo convierte en una lista de objetos manejables
    public List<Satellite> parse(String rawJson) {
        List<Satellite> satellites = new ArrayList<>();
        JsonArray starlinks = JsonParser.parseString(rawJson).getAsJsonArray();

        for (int i = 0; i < starlinks.size(); i++) {
            JsonObject obj = starlinks.get(i).getAsJsonObject();

            String name = "Desconocido";
            if (obj.has("spaceTrack") && !obj.get("spaceTrack").isJsonNull()) {
                name = obj.get("spaceTrack").getAsJsonObject().get("OBJECT_NAME").getAsString();
            } else if (obj.has("id")) {
                name = obj.get("id").getAsString();
            }

            // Creamos un nuevo objeto Satellite y lo añadimos a la lista
            satellites.add(new Satellite(name, obj.toString()));
        }
        return satellites;
    }
}