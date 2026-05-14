package org.ulpgc.starlink.monitor.control;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ulpgc.starlink.monitor.model.SatelliteEvent;
import org.ulpgc.starlink.monitor.model.WeatherEvent;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDataMart implements DataMart {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteDataMart.class);
    private final String dbUrl;
    private Connection connection;
    private PreparedStatement insertSatelliteStmt;
    private PreparedStatement insertWeatherCurrentStmt;
    private PreparedStatement insertWeatherHistoryStmt;
    private PreparedStatement updateHealthStmt;

    public SQLiteDataMart(String dbUrl) {
        this.dbUrl = dbUrl.startsWith("jdbc:sqlite:") ? dbUrl : "jdbc:sqlite:" + dbUrl;
        try {
            this.connection = DriverManager.getConnection(this.dbUrl);
            configureDatabase();
            initTables();
            prepareStatements();
        } catch (SQLException e) {
            logger.error("❌ Error conectando a SQLite: {}", e.getMessage());
        }
    }

    private void configureDatabase() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;"); // Enable WAL mode for better concurrency
            stmt.execute("PRAGMA synchronous=NORMAL;"); // Recommended for WAL mode
            stmt.execute("PRAGMA temp_store=MEMORY;");
            stmt.execute("PRAGMA cache_size=-64000;"); // 64MB cache
            stmt.execute("PRAGMA busy_timeout=10000;");
        } catch (SQLException e) {
            logger.warn("⚠️ Error configuring SQLite: {}", e.getMessage());
        }
    }

    private void prepareStatements() throws SQLException {
        insertSatelliteStmt = connection.prepareStatement("INSERT OR REPLACE INTO satellites VALUES (?, ?, ?, ?)");
        insertWeatherCurrentStmt = connection.prepareStatement("INSERT OR REPLACE INTO weather VALUES (?, ?, ?, ?, ?)");
        insertWeatherHistoryStmt = connection.prepareStatement("INSERT OR IGNORE INTO weather_history VALUES (?, ?, ?)");
        updateHealthStmt = connection.prepareStatement("INSERT OR REPLACE INTO service_health VALUES (?, ?)");
    }

    public void initTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS satellites (id TEXT PRIMARY KEY, latitude REAL, longitude REAL, timestamp TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS weather (location TEXT PRIMARY KEY, description TEXT, temperature REAL, humidity INTEGER, timestamp TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS weather_history (location TEXT, temp REAL, timestamp TEXT, PRIMARY KEY (location, timestamp))");
            stmt.execute("CREATE TABLE IF NOT EXISTS service_health (service_name TEXT PRIMARY KEY, last_seen TEXT)");
            
            // Indices for improved query performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_weather_history_loc_ts ON weather_history(location, timestamp DESC)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_satellites_ts ON satellites(timestamp DESC)");
            
            logger.info("🗄️ Datamart structure verified with indices.");
        } catch (SQLException e) {
            logger.error("❌ Error SQLite al crear tablas: {}", e.getMessage());
        }
    }

    public void beginTransaction() {
        try { connection.setAutoCommit(false); } catch (SQLException e) { logger.error("Error start transaction: {}", e.getMessage()); }
    }

    public void commitTransaction() {
        try { connection.commit(); connection.setAutoCommit(true); } catch (SQLException e) { logger.error("Error commit: {}", e.getMessage()); }
    }

    public synchronized void updateServiceHealth(String serviceName, String timestamp) {
        try {
            updateHealthStmt.setString(1, serviceName);
            updateHealthStmt.setString(2, timestamp);
            updateHealthStmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error salud: {}", e.getMessage());
        }
    }

    public synchronized String getServiceLastSeen(String serviceName) {
        String sql = "SELECT last_seen FROM service_health WHERE service_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, serviceName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("last_seen");
        } catch (SQLException e) { logger.error("Error getServiceLastSeen: {}", e.getMessage()); }
        return null;
    }

    public synchronized void save(JsonObject event) {
        try {
            if (event == null || !event.has("ss") || !event.has("ts")) return;
            String ss = event.get("ss").getAsString().toLowerCase();
            String ts = event.get("ts").getAsString();

            if (ss.contains("weather")) {
                WeatherEvent w = parseWeather(event, ss, ts);
                if (w != null) addWeather(w);
            } else if (ss.contains("spacex") || ss.contains("satellite")) {
                SatelliteEvent s = parseSatellite(event, ss, ts);
                if (s != null) addSatellite(s);
            }
        } catch (Exception e) {}
    }

    private WeatherEvent parseWeather(JsonObject event, String ss, String ts) {
        String location = event.has("name") ? event.get("name").getAsString() : (event.has("location") ? event.get("location").getAsString() : null);
        if (location == null || !event.has("temp")) return null;
        return new WeatherEvent(ts, ss, 0, event.get("temp").getAsDouble(), event.has("humidity") ? event.get("humidity").getAsInt() : 0, 0.0, event.has("description") ? event.get("description").getAsString() : "Unknown", location, 0.0, 0.0);
    }

    private SatelliteEvent parseSatellite(JsonObject event, String ss, String ts) {
        String id = event.has("satelliteId") ? event.get("satelliteId").getAsString() : (event.has("id") ? event.get("id").getAsString() : null);
        if (id == null) return null;
        double lat = event.has("latitude") ? event.get("latitude").getAsDouble() : (event.has("lat") ? event.get("lat").getAsDouble() : 0.0);
        double lon = event.has("longitude") ? event.get("longitude").getAsDouble() : (event.has("lon") ? event.get("lon").getAsDouble() : 0.0);
        return new SatelliteEvent(ts, ss, id, lat, lon);
    }

    public synchronized void addWeather(WeatherEvent w) {
        try {
            insertWeatherCurrentStmt.setString(1, w.locationName());
            insertWeatherCurrentStmt.setString(2, w.description());
            insertWeatherCurrentStmt.setDouble(3, w.temperature());
            insertWeatherCurrentStmt.setInt(4, w.humidity());
            insertWeatherCurrentStmt.setString(5, w.timestamp());
            insertWeatherCurrentStmt.executeUpdate();

            insertWeatherHistoryStmt.setString(1, w.locationName());
            insertWeatherHistoryStmt.setDouble(2, w.temperature());
            insertWeatherHistoryStmt.setString(3, w.timestamp());
            insertWeatherHistoryStmt.executeUpdate();
        } catch (SQLException e) {}
    }

    public synchronized void addSatellite(SatelliteEvent sat) {
        try {
            insertSatelliteStmt.setString(1, sat.id());
            insertSatelliteStmt.setDouble(2, sat.latitude());
            insertSatelliteStmt.setDouble(3, sat.longitude());
            insertSatelliteStmt.setString(4, sat.timestamp());
            insertSatelliteStmt.executeUpdate();
        } catch (SQLException e) {}
    }

    public synchronized List<Double> getTempHistory(String location) {
        List<Double> temps = new ArrayList<>();
        String sql = "SELECT temp FROM weather_history WHERE location LIKE ? ORDER BY timestamp DESC LIMIT 10";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + location + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) temps.add(rs.getDouble("temp"));
        } catch (SQLException e) { e.printStackTrace(); }
        return temps;
    }

    public synchronized WeatherEvent getLatestWeather(String location) {
        return getLatestWeatherByTime(location, Instant.now().toString());
    }

    public synchronized WeatherEvent getLatestWeatherByTime(String location, String maxTimestamp) {
        String sql = "SELECT * FROM weather WHERE location LIKE ? AND timestamp <= ? ORDER BY timestamp DESC LIMIT 1; ";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + location + "%");
            pstmt.setString(2, maxTimestamp);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new WeatherEvent(rs.getString("timestamp"), "SQLite", 0, rs.getDouble("temperature"), rs.getInt("humidity"), 0.0, rs.getString("description"), rs.getString("location"), 0.0, 0.0);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public synchronized List<SatelliteEvent> getActiveSatellitesByTime(int limit, String maxTimestamp) {
        List<SatelliteEvent> satellites = new ArrayList<>();
        String sql = "SELECT * FROM satellites WHERE timestamp <= ? ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, maxTimestamp);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                satellites.add(new SatelliteEvent(rs.getString("timestamp"), "SQLite", rs.getString("id"), rs.getDouble("latitude"), rs.getDouble("longitude")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return satellites;
    }

    public synchronized List<SatelliteEvent> getActiveSatellites(int limit) {
        return getActiveSatellitesByTime(limit, Instant.now().toString());
    }
}
