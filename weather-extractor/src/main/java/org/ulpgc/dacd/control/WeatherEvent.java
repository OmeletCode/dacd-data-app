package org.ulpgc.dacd.control; // Cambia esto si tu clase está en org.ulpgc.dacd.model

public class WeatherEvent {
    // 1. Requisitos mínimos obligatorios (Metadatos)
    private String ts; // El timestamp de cuando se creó el evento en UTC
    private String ss; // El nombre del feeder (ej: "Weather-Feeder")

    // 2. El Payload (Los datos que sacamos de OpenWeatherMap)
    private long dt;
    private double temp;
    private int humidity;
    private double windSpeed;
    private String description;
    private String name;
    private double lat;
    private double lon;

    // --- CONSTRUCTOR ---
    public WeatherEvent(String ts, String String, long dt, double temp, int humidity, double windSpeed, String description, String name, double lat, double lon) {
        this.ts = ts;
        this.ss = String; // Fíjate que le paso el identificador del feeder
        this.dt = dt;
        this.temp = temp;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.description = description;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    // --- GETTERS ---
    public String getTs() {return ts;}
    public String getSs() {return ss;}
    public long getDt() {return dt;}
    public double getTemp() {return temp;}
    public int getHumidity() {return humidity;}
    public double getWindSpeed() {return windSpeed;}
    public String getDescription() {return description;}
    public String getName() {return name;}
    public double getLat() {return lat;}
    public double getLon() {return lon;}
}