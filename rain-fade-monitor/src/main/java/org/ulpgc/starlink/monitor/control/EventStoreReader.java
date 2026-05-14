package org.ulpgc.starlink.monitor.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public record EventStoreReader(String rootPath) {

    public void readAndProcessEvents(java.util.function.Consumer<JsonObject> consumer) {
        File root = new File(rootPath);
        if (!root.exists() || !root.isDirectory()) return;

        File[] typeFolders = root.listFiles(File::isDirectory);
        if (typeFolders == null) return;

        for (File typeFolder : typeFolders) {
            File[] providerFolders = typeFolder.listFiles(File::isDirectory);
            if (providerFolders == null) continue;

            for (File providerFolder : providerFolders) {
                File[] eventFiles = providerFolder.listFiles((dir, name) -> name.endsWith(".events"));
                if (eventFiles == null) continue;

                // Ordenar archivos por nombre DESCENDENTE (más recientes primero)
                java.util.Arrays.sort(eventFiles, java.util.Comparator.comparing(File::getName).reversed());

                for (File eventFile : eventFiles) {
                    processEventsFromFile(eventFile, consumer);
                }
            }
        }
    }

    private void processEventsFromFile(File file, java.util.function.Consumer<JsonObject> consumer) {
        try (java.util.stream.Stream<String> lines = Files.lines(file.toPath())) {
            lines.forEach(line -> {
                try {
                    consumer.accept(JsonParser.parseString(line).getAsJsonObject());
                } catch (Exception e) {
                    // Ignorar líneas mal formadas
                }
            });
        } catch (IOException e) {
            System.err.println("Error leyendo archivo: " + file.getName());
        }
    }
}
