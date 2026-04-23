package org.ulpgc.dacd.model;

import com.google.gson.annotations.SerializedName;

public record SatelliteEvent(
        @SerializedName("ts") String ts,
        @SerializedName("ss") String ss,
        @SerializedName("satelliteId") String id,
        @SerializedName("latitude") double lat,
        @SerializedName("longitude") double lon
    ) {}