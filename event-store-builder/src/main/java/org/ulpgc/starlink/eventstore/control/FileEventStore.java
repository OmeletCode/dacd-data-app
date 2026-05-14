package org.ulpgc.starlink.eventstore.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FileEventStore {
    private static final Logger logger = LoggerFactory.getLogger(FileEventStore.class);
    private final String baseDirectory = "eventstore";

    public void save(String topic, String jsonEvent) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonEvent).getAsJsonObject();
            String ss = jsonObject.get("ss").getAsString();
            String ts = jsonObject.get("ts").getAsString();

            Instant instant = Instant.parse(ts);
            LocalDate date = LocalDate.ofInstant(instant, ZoneOffset.UTC);
            String dateString = date.format(DateTimeFormatter.BASIC_ISO_DATE);

            Path dirPath = Paths.get(baseDirectory, topic, ss);
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(dateString + ".events");

            Files.writeString(filePath, jsonEvent + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (Exception e) {
            logger.error("Error al intentar guardar el evento en disco: {}", e.getMessage());
        }
    }
}
