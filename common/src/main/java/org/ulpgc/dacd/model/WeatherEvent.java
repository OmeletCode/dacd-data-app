package org.ulpgc.dacd.model;

import com.google.gson.annotations.SerializedName;

public record WeatherEvent(
        @SerializedName("ts") String timestamp,
        @SerializedName("ss") String sourceSystem,
        @SerializedName("dt") long observationTime, // dt suele ser el Unix Timestamp de la predicción
        @SerializedName("temp") double temperature,
        @SerializedName("humidity") int humidity,
        @SerializedName("windSpeed") double windSpeed,
        @SerializedName("description") String description,
        @SerializedName("name") String locationName, // 'name' es muy genérico, locationName da contexto
        @SerializedName("lat") double latitude,
        @SerializedName("lon") double longitude
) {}