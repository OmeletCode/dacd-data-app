package org.ulpgc.dacd.repository;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.model.SatelliteEvent;

public class MemoryDataMart {
    private final List<WeatherEvent> weatherEvents = new CopyOnWriteArrayList<>();
    private final List<SatelliteEvent> satelliteEvents = new CopyOnWriteArrayList<>();

    public void addWeather(WeatherEvent event) {
        weatherEvents.add(event);
    }

    public void addSatellite(SatelliteEvent event) {
        satelliteEvents.add(event);
    }

    public List<WeatherEvent> getWeatherEvents() {
        return Collections.unmodifiableList(weatherEvents);
    }

    public List<SatelliteEvent> getSatelliteEvents() {
        return Collections.unmodifiableList(satelliteEvents);
    }
}