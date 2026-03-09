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

            // 1. Tabla para SpaceX
            String sqlSpaceX = "CREATE TABLE IF NOT EXISTS starlink_raw (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "satellite_name TEXT," +
                    "captured_at TEXT," +
                    "json_content TEXT" +
                    ");";
            stmt.execute(sqlSpaceX);

            // 2. Tabla para Clima
            String sqlWeather = "CREATE TABLE IF NOT EXISTS weather_raw (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "location TEXT," +
                    "api_timestamp INTEGER," +
                    "temp REAL," +
                    "humidity INTEGER," +
                    "wind_speed REAL," +
                    "description TEXT," +
                    "captured_at TEXT" +
                    ");";
            stmt.execute(sqlWeather);

        } catch (SQLException e) {
            System.err.println("Error inicializando DB: " + e.getMessage());
        }
    }

    // Método para tus satélites
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
            System.err.println("Error al insertar satélite: " + e.getMessage());
        }
    }

    // Método nuevo para el clima
    public void saveWeather(long timestamp, double temp, int humidity, double windSpeed, String description, String location) {
        String sql = "INSERT INTO weather_raw(location, api_timestamp, temp, humidity, wind_speed, description, captured_at) " +
                "VALUES(?,?,?,?,?,?,?)";
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Usamos las variables directas
            pstmt.setString(1, location);
            pstmt.setLong(2, timestamp);
            pstmt.setDouble(3, temp);
            pstmt.setInt(4, humidity);
            pstmt.setDouble(5, windSpeed);
            pstmt.setString(6, description);
            pstmt.setString(7, now);

            pstmt.executeUpdate();
            System.out.println("-> [DB] Clima de " + location + " guardado.");
        } catch (SQLException e) {
            System.err.println("Error al insertar clima: " + e.getMessage());
        }
    }
}