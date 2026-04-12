package org.ulpgc.dacd.control;

import com.google.gson.Gson;

public class GsonEventSerializer {
    private final Gson gson;

    public GsonEventSerializer() {
        this.gson = new Gson();
    }

    public String serialize(Object event) {
        return gson.toJson(event);
    }
}