package org.ulpgc.dacd.business_unit.repository;

import org.ulpgc.dacd.business_unit.model.SatelliteEvent;
import org.ulpgc.dacd.business_unit.model.WeatherEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public record SQLiteDataMart(String dbUrl) {
    public SQLiteDataMart(String dbUrl) {
        this.dbUrl = "jdbc:sqlite:" + dbUrl;
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            // Tabla de satélites
            stmt.execute("CREATE TABLE IF NOT EXISTS satellites (id TEXT PRIMARY KEY, latitude REAL, longitude REAL, timestamp TEXT)");
            // Tabla de clima actual
            stmt.execute("CREATE TABLE IF NOT EXISTS weather (location TEXT PRIMARY KEY, description TEXT, temperature REAL, humidity INTEGER, timestamp TEXT)");
            // Tabla de historial para la gráfica
            stmt.execute("CREATE TABLE IF NOT EXISTS weather_history (location TEXT, temp REAL, timestamp TEXT)");
            System.out.println("🗄️ Datamart SQLite inicializado correctamente.");
        } catch (SQLException e) {
            System.err.println("❌ Error SQLite: " + e.getMessage());
        }
    }

    public void addWeather(WeatherEvent w) {
        String sqlCurrent = "INSERT OR REPLACE INTO weather (location, description, temperature, humidity, timestamp) VALUES (?, ?, ?, ?, ?)";
        String sqlHistory = "INSERT INTO weather_history (location, temp, timestamp) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCurrent)) {
                pstmt.setString(1, w.locationName());
                pstmt.setString(2, w.description());
                pstmt.setDouble(3, w.temperature());
                pstmt.setInt(4, w.humidity());
                pstmt.setString(5, w.timestamp());
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sqlHistory)) {
                pstmt.setString(1, w.locationName());
                pstmt.setDouble(2, w.temperature());
                pstmt.setString(3, w.timestamp());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error persistiendo clima: " + e.getMessage());
        }
    }

    public List<Double> getTempHistory(String location) {
        List<Double> temps = new ArrayList<>();
        String sql = "SELECT temp FROM weather_history WHERE location LIKE ? ORDER BY timestamp DESC LIMIT 10";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + location + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) temps.add(rs.getDouble("temp"));
        } catch (SQLException e) { e.printStackTrace(); }
        return temps;
    }

    // Novedad: Método en singular para cuando ActiveMQ escupe de 1 en 1
    public void addSatellite(SatelliteEvent sat) {
        String sql = "INSERT OR REPLACE INTO satellites (id, latitude, longitude, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sat.id());
            pstmt.setDouble(2, sat.latitude());
            pstmt.setDouble(3, sat.longitude());
            pstmt.setString(4, sat.timestamp());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error insertando satélite en tiempo real: " + e.getMessage());
        }
    }

    public void addSatellites(List<SatelliteEvent> satellites) {
        String sql = "INSERT OR REPLACE INTO satellites (id, latitude, longitude, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (SatelliteEvent sat : satellites) {
                    pstmt.setString(1, sat.id());
                    pstmt.setDouble(2, sat.latitude());
                    pstmt.setDouble(3, sat.longitude());
                    pstmt.setString(4, sat.timestamp());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
            }
        } catch (SQLException e) { System.err.println("Error en lote de satélites: " + e.getMessage()); }
    }

    public WeatherEvent getLatestWeather(String location) {
        String sql = "SELECT * FROM weather WHERE location LIKE ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + location + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new WeatherEvent(rs.getString("timestamp"), "SQLite", 0, rs.getDouble("temperature"), rs.getInt("humidity"), 0.0, rs.getString("description"), rs.getString("location"), 0.0, 0.0);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<SatelliteEvent> getActiveSatellites(int limit) {
        List<SatelliteEvent> satellites = new ArrayList<>();
        String sql = "SELECT * FROM satellites LIMIT ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                satellites.add(new SatelliteEvent(rs.getString("timestamp"), "SQLite", rs.getString("id"), rs.getDouble("latitude"), rs.getDouble("longitude")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return satellites;
    }
}