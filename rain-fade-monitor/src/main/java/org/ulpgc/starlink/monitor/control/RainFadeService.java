package org.ulpgc.starlink.monitor.control;

import org.ulpgc.starlink.monitor.model.RainFadeResponse;
import org.ulpgc.starlink.monitor.model.SatelliteEvent;
import org.ulpgc.starlink.monitor.model.WeatherEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record RainFadeService(DataMart dataMart) {
    private static final double KU_BAND_FACTOR_A = 0.0188;
    private static final double KU_BAND_FACTOR_B = 1.15;
    private static final double RAIN_HEIGHT_KM = 4.0; // Altura promedio de la capa de lluvia
    
    private static final Map<String, double[]> ISLAND_COORDS = Map.of(
        "Las Palmas", new double[]{28.1235, -15.4363},
        "London", new double[]{51.5074, -0.1278},
        "Dubai", new double[]{25.2048, 55.2708},
        "Singapore", new double[]{1.3521, 103.8198},
        "Seattle", new double[]{47.6062, -122.3321}
    );

    public Map<String, String> getServiceHealth() {
        Map<String, String> health = new HashMap<>();
        // Registramos nombres que coincidan exactamente con lo que llega (en minúsculas en DB)
        health.put("SpaceX-Feeder", checkService("spacex-feeder", 300)); // 5 min
        health.put("Weather-Feeder", checkService("weather-feeder", 1200)); // 20 min
        return health;
    }

    private String checkService(String name, int secondsThreshold) {
        String lastSeenStr = dataMart.getServiceLastSeen(name);
        if (lastSeenStr == null) return "FAIL";
        try {
            // Si el satélite se mueve, el mensaje está llegando.
            // A veces el reloj del contenedor o el TS del evento tienen desfase.
            Instant lastSeen = Instant.parse(lastSeenStr);
            long diffSeconds = Math.abs(Instant.now().getEpochSecond() - lastSeen.getEpochSecond());
            
            if (diffSeconds < secondsThreshold) {
                return "OK";
            }
        } catch (Exception e) {
            System.err.println("Error parseando salud para " + name + ": " + e.getMessage());
        }
        return "FAIL";
    }

    public List<RainFadeResponse.Prediction> getPredictionsForLocation(String location) {
        return getPredictionsForLocationAt(location, Instant.now().toString());
    }

    public List<RainFadeResponse.Prediction> getPredictionsForLocationAt(String location, String timestamp) {
        WeatherEvent weather = dataMart.getLatestWeatherByTime(location, timestamp);
        if (weather == null) return List.of();

        double[] origin = ISLAND_COORDS.getOrDefault(location, new double[]{0, 0});
        List<SatelliteEvent> rawSatellites = dataMart.getActiveSatellitesByTime(800, timestamp);
        
        double rainRate = estimateRainRateMmPerHour(weather.description());
        
        List<RainFadeResponse.SatelliteInfo> satellites = rawSatellites.parallelStream()
                .map(s -> calculateSatelliteStats(s, origin, rainRate))
                .toList();

        List<Double> tempHistory = dataMart.getTempHistory(location);
        double maxAtt = satellites.stream().mapToDouble(RainFadeResponse.SatelliteInfo::attenuationDb).max().orElse(0.1);

        return List.of(new RainFadeResponse.Prediction(
                "ITU-R P.618 Model",
                new RainFadeResponse.WeatherInfo(weather.temperature(), weather.humidity(), 0, weather.description()),
                satellites,
                determineRisk(maxAtt),
                tempHistory,
                maxAtt
        ));
    }

    private RainFadeResponse.SatelliteInfo calculateSatelliteStats(SatelliteEvent s, double[] origin, double rainRate) {
        double elevation = calculateElevation(origin[0], origin[1], s.latitude(), s.longitude());
        
        // Base de atenuación atmosférica (incluso sin lluvia hay una pequeña pérdida)
        double attenuation = 0.05 + (Math.random() * 0.05); 
        
        if (elevation > 5) { 
            double slantPath = RAIN_HEIGHT_KM / Math.sin(Math.toRadians(elevation));
            // Si hay lluvia, sumamos la atenuación por lluvia
            if (rainRate > 0) {
                double gamma = KU_BAND_FACTOR_A * Math.pow(rainRate, KU_BAND_FACTOR_B);
                attenuation += (gamma * slantPath);
            } else if (elevation < 20) {
                // Si el satélite está muy bajo, la atmósfera misma atenúa más
                attenuation += (0.2 * (20 - elevation) / 20);
            }
        }

        return new RainFadeResponse.SatelliteInfo(s.id(), s.latitude(), s.longitude(), attenuation, elevation);
    }

    String determineRisk(double db) {
        if (db >= 3.0) return "HIGH";
        if (db > 0.8) return "MEDIUM";
        return "LOW";
    }

    private double calculateElevation(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // Radio de la Tierra en km
        double h = 550.0;  // Altura órbita Starlink en km
        
        double d = calculateDistance(lat1, lon1, lat2, lon2); // Distancia en superficie en km

        // Geometría del satélite: estimación del ángulo de elevación
        // epsilon = arctan( (h * (2R + h) - d^2) / (2d * (R + h)) ) - aproximación
        double elevation = Math.toDegrees(Math.atan((h / d) - (d / (2 * R))));
        
        return Math.max(0, Math.min(90, elevation));
    }

    double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private double estimateRainRateMmPerHour(String description) {
        if (description == null) return 0.0;
        String desc = description.toLowerCase();
        if (desc.contains("heavy") || desc.contains("extreme") || desc.contains("thunderstorm")) return 25.0;
        if (desc.contains("moderate") || desc.equals("rain")) return 8.0;
        if (desc.contains("light") || desc.contains("drizzle")) return 2.0;
        return 0.0;
    }
}
