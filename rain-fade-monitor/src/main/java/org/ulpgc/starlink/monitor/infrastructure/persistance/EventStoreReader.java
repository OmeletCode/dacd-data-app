package org.ulpgc.starlink.monitor.infrastructure.persistance;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class EventStoreReader {
    private final String rootPath;
    private final Gson gson;

    public EventStoreReader(String rootPath) {
        this.rootPath = rootPath;
        this.gson = new Gson();
    }

    public List<JsonObject> readAllEvents() {
        List<JsonObject> allEvents = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(rootPath))) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".events"))
                    .forEach(path -> {
                        allEvents.addAll(readEventsFromFile(path.toFile()));
                    });
        } catch (IOException e) {
            System.err.println("Error recorriendo el event store: " + e.getMessage());
        }
        return allEvents;
    }

    private List<JsonObject> readEventsFromFile(File file) {
        List<JsonObject> events = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    events.add(gson.fromJson(line, JsonObject.class));
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo archivo " + file.getName() + ": " + e.getMessage());
        }
        return events;
    }
}