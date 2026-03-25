package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Weather;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteWeatherSerializer implements WeatherSerializer {
    private final String dbUrl;

    public SqliteWeatherSerializer(String dbUrl) {
        this.dbUrl = dbUrl;
        initDatabase(); // Nos aseguramos de que la tabla exista al arrancar
    }

    // Método que crea la tabla con las columnas exactas que necesitamos
    private void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS weather_raw (" +
                "ts INTEGER, " +
                "temp REAL, " +
                "humidity INTEGER, " +
                "windSpeed REAL, " +
                "description TEXT, " +
                "location TEXT" +
                ");";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creando la tabla: " + e.getMessage());
        }
    }

    @Override
    public void saveWeather(Weather weather) {
        String sql = "INSERT INTO weather_raw (ts, temp, humidity, windSpeed, description, location) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, weather.timestamp());
            pstmt.setDouble(2, weather.temp());
            pstmt.setInt(3, weather.humidity());
            pstmt.setDouble(4, weather.windSpeed());
            pstmt.setString(5, weather.description());
            pstmt.setString(6, weather.location());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error guardando en la base de datos: " + e.getMessage());
        }
    }
}