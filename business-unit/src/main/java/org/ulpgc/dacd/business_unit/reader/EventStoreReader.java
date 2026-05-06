package org.ulpgc.dacd.business_unit.reader;

import com.google.gson.Gson;
import org.ulpgc.dacd.business_unit.model.WeatherEvent;
import org.ulpgc.dacd.business_unit.model.SatelliteEvent;

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
        List<WeatherEvent> events = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                WeatherEvent event = gson.fromJson(line, WeatherEvent.class);
                events.add(event);
            }
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo " + filePath + ": " + e.getMessage());
        }

        return events;
    }

public List<SatelliteEvent> readSatelliteEvents(String filePath) {
    List<SatelliteEvent> events = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        while ((line = br.readLine()) != null) {
            events.add(gson.fromJson(line, SatelliteEvent.class));
        }
    } catch (IOException e) {
        System.err.println("Error leyendo satélites: " + e.getMessage());
    }
    return events;
}
}