package org.ulpgc.dacd.model;

import com.google.gson.annotations.SerializedName;

public record WeatherEvent(
        @SerializedName("ts") String timestamp,
        @SerializedName("name") String location,
        @SerializedName("lat") double lat,
        @SerializedName("lon") double lon,
        @SerializedName("temp") double temperature,
        @SerializedName("humidity") int humidity,
        @SerializedName("windSpeed") double windSpeed,
        @SerializedName("description") String description
) {}