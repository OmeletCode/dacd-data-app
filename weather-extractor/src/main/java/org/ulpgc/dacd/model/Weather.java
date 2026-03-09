package org.ulpgc.dacd.model;
import java.util.Date;

public class Weather {
    private long timestamp;
    private double temp;
    private int humidity;
    private double windSpeed;
    private String description;
    private String location;

    public Weather(long timestamp, double temp, int humidity, double windSpeed, String description, String location) {
        this.timestamp = timestamp;
        this.temp = temp;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.description = description;
        this.location = location;
    }

    @Override
    public String toString() {
        Date fecha = new Date(timestamp * 1000);
        return "Datos de: " + location + "\n" +
                "Fecha: " + fecha + "\n" +
                "Temperatura: " + temp + "°C\n" +
                "Humedad: " + humidity + "%\n" +
                "Viento: " + windSpeed + " m/s\n" +
                "Cielo: " + description;
    }
}
