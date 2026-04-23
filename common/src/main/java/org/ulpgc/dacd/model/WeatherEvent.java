package org.ulpgc.dacd.model;

import com.google.gson.annotations.SerializedName;

public record WeatherEvent(
         String ts,
         String ss,
         long dt,
         double temp,
         int humidity,
         double windSpeed,
         String description,
         String name,
        double lat,
        double lon
    ) {}