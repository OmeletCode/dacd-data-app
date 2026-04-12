package org.ulpgc.dacd;

public class SpaceXEvent {
// 1. Requisitos mínimos obligatorios
    private String ts; // El timestamp (hora de captura)
    private String ss; // El nombre del feeder (ej: "SpaceX")

    private String satelliteId;
    private double latitude;
    private double longitude;

    public SpaceXEvent(String ts, String ss, String satelliteId, double latitude, double longitude) {
        this.ts = ts;
        this.ss = ss;
        this.satelliteId = satelliteId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTs() { return ts; }
    public String getSs() { return ss; }
    public String getSatelliteId() { return satelliteId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

}

