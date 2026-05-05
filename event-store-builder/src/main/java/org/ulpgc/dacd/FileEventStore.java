package org.ulpgc.dacd;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FileEventStore {
    private static final String SOURCE_SYSTEM_KEY = "ss";
    private static final String TIMESTAMP_KEY = "ts";
    private static final String FILE_EXTENSION = ".events";

    private final String baseDirectory;

    public FileEventStore() {
        this("eventstore"); // Directorio por defecto
    }

    public FileEventStore(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public void save(String topic, String jsonEvent) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonEvent).getAsJsonObject();
            String sourceSystem = extractJsonField(jsonObject, SOURCE_SYSTEM_KEY);
            String timestamp = extractJsonField(jsonObject, TIMESTAMP_KEY);

            String dateString = formatTimestampToBasicIsoDate(timestamp);
            Path filePath = buildAndPrepareFilePath(topic, sourceSystem, dateString);

            appendEventToFile(filePath, jsonEvent);

        } catch (Exception e) {
            System.err.println("❌ Error al guardar el evento en disco: " + e.getMessage());
        }
    }

    private String extractJsonField(JsonObject jsonObject, String key) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsString();
        }
        throw new IllegalArgumentException("El evento no contiene el campo obligatorio: " + key);
    }

    private String formatTimestampToBasicIsoDate(String timestamp) {
        Instant instant = Instant.parse(timestamp);
        LocalDate date = LocalDate.ofInstant(instant, ZoneOffset.UTC);
        return date.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private Path buildAndPrepareFilePath(String topic, String sourceSystem, String dateString) throws IOException {
        Path dirPath = Paths.get(baseDirectory, topic, sourceSystem);
        Files.createDirectories(dirPath);
        return dirPath.resolve(dateString + FILE_EXTENSION);
    }

    private void appendEventToFile(Path filePath, String jsonEvent) throws IOException {
        Files.writeString(
                filePath,
                jsonEvent + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
        System.out.println("💾 Evento guardado en: " + filePath);
    }
}