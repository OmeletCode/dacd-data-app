package org.ulpgc.dacd.reader;

import com.google.gson.Gson;
import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.model.SatelliteEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventStoreReader {
    private final Gson gson;

    public EventStoreReader() {
        this.gson = new Gson();
    }

    public List<WeatherEvent> readWeatherEvents(String filePath) {
        return readEventsFromFile(filePath, WeatherEvent.class);
    }

    public List<SatelliteEvent> readSatelliteEvents(String filePath) {
        return readEventsFromFile(filePath, SatelliteEvent.class);
    }

    private <T> List<T> readEventsFromFile(String filePath, Class<T> eventClass) {
        List<T> events = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                T event = gson.fromJson(line, eventClass);
                events.add(event);
            }
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo " + filePath + ": " + e.getMessage());
        }

        return events;
    }
}