package org.ulpgc.starlink.spacex.infrastructure.broker;

import com.google.gson.Gson;

public class GsonEventSerializer {

    private static final Gson GSON = new Gson();

    public GsonEventSerializer() {
    }

    public <T> String serialize(T event) {
        return GSON.toJson(event);
    }
}