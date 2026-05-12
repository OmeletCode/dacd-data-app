package org.ulpgc.wather_extractor.model;

import com.google.gson.annotations.SerializedName;

public record WeatherEvent(
        @SerializedName("ts") String timestamp,
        @SerializedName("ss") String sourceSystem,
        @SerializedName("temp") double temperature,
        @SerializedName("humidity") int humidity,
        @SerializedName("description") String description,
        @SerializedName("name") String locationName
) {}