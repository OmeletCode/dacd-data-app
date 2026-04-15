package org.ulpgc.dacd;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FileEventStore {
    private final String baseDirectory = "eventstore";

    public void save(String topic, String jsonEvent) {
        try {
            // 1. Extraemos los metadatos (ss y ts) leyendo el JSON con Gson
            JsonObject jsonObject = JsonParser.parseString(jsonEvent).getAsJsonObject();
            String ss = jsonObject.get("ss").getAsString();
            String ts = jsonObject.get("ts").getAsString();

            // 2. Transformamos el Timestamp (ej: "2026-04-15T10:00:00Z") al formato YYYYMMDD
            Instant instant = Instant.parse(ts);
            LocalDate date = LocalDate.ofInstant(instant, ZoneOffset.UTC);
            String dateString = date.format(DateTimeFormatter.BASIC_ISO_DATE);

            // 3. Construimos la ruta de las carpetas: eventstore/{topic}/{ss}
            Path dirPath = Paths.get(baseDirectory, topic, ss);

            // Files.createDirectories es magia: si la carpeta ya existe, no hace nada. Si no, la crea.
            Files.createDirectories(dirPath);

            // 4. Preparamos el archivo: {YYYYMMDD}.events
            Path filePath = dirPath.resolve(dateString + ".events");

            // 5. Escribimos el JSON añadiendo un salto de línea al final (APPEND = añadir, no borrar)
            Files.writeString(filePath, jsonEvent + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            System.out.println("💾 Evento guardado en: " + filePath);

        } catch (Exception e) {
            System.err.println("Error al intentar guardar el evento en disco: " + e.getMessage());
        }
    }
}