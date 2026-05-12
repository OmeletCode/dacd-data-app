package org.ulpgc.dacd.business_unit;

import org.ulpgc.dacd.business_unit.model.RainFadeResponse;
import org.ulpgc.dacd.business_unit.model.SatelliteEvent;
import org.ulpgc.dacd.business_unit.model.WeatherEvent;
import org.ulpgc.dacd.business_unit.repository.SQLiteDataMart;

import java.util.List;

public record RainFadeService(SQLiteDataMart dataMart) {
    private static final double KU_BAND_FACTOR_A = 0.0188;
    private static final double KU_BAND_FACTOR_B = 1.15;
    private static final double HIGH_RISK_THRESHOLD_DB = 3.0;
    private static final double MEDIUM_RISK_THRESHOLD_DB = 0.5;

    public List<RainFadeResponse.Prediction> getPredictionsForLocation(String location) {
        WeatherEvent weather = dataMart.getLatestWeather(location);

        if (weather == null) {
            return List.of();
        }

        List<SatelliteEvent> rawSatellites = dataMart.getActiveSatellites(200);

        List<RainFadeResponse.SatelliteInfo> activeSatellites = rawSatellites.stream()
                .map(s -> new RainFadeResponse.SatelliteInfo(s.id(), s.latitude(), s.longitude()))
                .toList();

        // 📈 NUEVO: Pedimos el historial de temperaturas a la base de datos
        List<Double> tempHistory = dataMart.getTempHistory(location);

        return buildPredictions(weather, activeSatellites, tempHistory);
    }

    private List<RainFadeResponse.Prediction> buildPredictions(WeatherEvent weather,
                                                               List<RainFadeResponse.SatelliteInfo> satellites,
                                                               List<Double> tempHistory) {
        String risk = calculateRainFadeRisk(weather.description());
        RainFadeResponse.WeatherInfo weatherInfo = new RainFadeResponse.WeatherInfo(
                weather.temperature(), weather.humidity(), 0, weather.description()
        );

        // Pasamos el historial al objeto de respuesta que va hacia el WebSocket
        return List.of(new RainFadeResponse.Prediction(
                "Radar SQL en vivo",
                weatherInfo,
                satellites,
                risk,
                tempHistory
        ));
    }

    private String calculateRainFadeRisk(String weatherDescription) {
        double rainRate = estimateRainRateMmPerHour(weatherDescription);
        double attenuationDb = KU_BAND_FACTOR_A * Math.pow(rainRate, KU_BAND_FACTOR_B);

        if (attenuationDb > HIGH_RISK_THRESHOLD_DB) return "HIGH";
        if (attenuationDb > MEDIUM_RISK_THRESHOLD_DB) return "MEDIUM";
        if (weatherDescription != null && weatherDescription.toLowerCase().contains("clouds")) return "MEDIUM";

        return "LOW";
    }

    private double estimateRainRateMmPerHour(String description) {
        if (description == null) return 0.0;
        String desc = description.toLowerCase();
        if (desc.contains("heavy") || desc.contains("extreme") || desc.contains("thunderstorm")) return 25.0;
        if (desc.contains("moderate") || desc.equals("rain")) return 10.0;
        if (desc.contains("light") || desc.contains("drizzle")) return 2.5;
        return 0.0;
    }
}