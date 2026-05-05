package org.ulpgc.dacd.model;

import com.google.gson.annotations.SerializedName;

public record SatelliteEvent(
        @SerializedName("ts") String timestamp,
        @SerializedName("ss") String sourceSystem,
        @SerializedName("satelliteId") String id,
        @SerializedName("latitude") double latitude,
        @SerializedName("longitude") double longitude
) {}