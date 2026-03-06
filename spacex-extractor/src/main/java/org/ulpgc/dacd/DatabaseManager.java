package org.ulpgc.dacd;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {
    private final String url = "jdbc:sqlite:spacex_data.db";

    public DatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // Creamos la tabla si no existe.
            // IMPORTANTE: captured_at para el análisis temporal
            String sql = "CREATE TABLE IF NOT EXISTS starlink_raw (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "satellite_name TEXT," +
                    "captured_at TEXT," +
                    "json_content TEXT" +
                    ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error inicializando DB: " + e.getMessage());
        }
    }

    public void saveSatellite(String name, String fullJson) {
        String sql = "INSERT INTO starlink_raw(satellite_name, captured_at, json_content) VALUES(?,?,?)";
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, now);
            pstmt.setString(3, fullJson);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al insertar: " + e.getMessage());
        }
    }
}