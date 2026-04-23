package org.ulpgc.dacd.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.model.SatelliteEvent;

public class MemoryDataMart {
    // CopyOnWriteArrayList evita que el programa colapse si la API lee
    // exactamente en el mismo milisegundo en el que ActiveMQ guarda un dato.
    private final List<WeatherEvent> weatherEvents = new CopyOnWriteArrayList<>();
    private final List<SatelliteEvent> satelliteEvents = new CopyOnWriteArrayList<>();

    public void addWeather(WeatherEvent event) {
        weatherEvents.add(event);
    }

    public void addSatellite(SatelliteEvent event) {
        satelliteEvents.add(event);
    }

    public List<WeatherEvent> getWeatherEvents() {
        return new ArrayList<>(weatherEvents);
    }

    public List<SatelliteEvent> getSatelliteEvents() {
        return new ArrayList<>(satelliteEvents);
    }
}