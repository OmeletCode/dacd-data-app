package org.ulpgc.dacd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SqliteSpaceXSerializer implements SpaceXSerializer {
    private final String dbUrl;

    public SqliteSpaceXSerializer(String dbUrl) {
        this.dbUrl = dbUrl;
        initDatabase();
    }

    private void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS starlink_raw (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "satellite_name TEXT," +
                "captured_at TEXT," +
                "json_content TEXT" +
                ");";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creando tabla SpaceX: " + e.getMessage());
        }
    }

    @Override
    public void saveSatellite(Satellite satellite) {
        String sql = "INSERT INTO starlink_raw(satellite_name, captured_at, json_content) VALUES(?,?,?)";
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, satellite.name());
            pstmt.setString(2, now);
            pstmt.setString(3, satellite.rawJson());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al insertar satélite: " + e.getMessage());
        }
    }
}