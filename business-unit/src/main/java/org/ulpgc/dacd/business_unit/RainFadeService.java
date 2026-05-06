package org.ulpgc.dacd.business_unit;

import org.ulpgc.dacd.business_unit.model.RainFadeResponse;
import org.ulpgc.dacd.business_unit.model.SatelliteEvent;
import org.ulpgc.dacd.business_unit.model.WeatherEvent;
import org.ulpgc.dacd.business_unit.repository.MemoryDataMart;

import java.util.List;
import java.util.stream.Collectors;

public class RainFadeService {
    private static final double KU_BAND_FACTOR_A = 0.0188;
    private static final double KU_BAND_FACTOR_B = 1.15;
    private static final double HIGH_RISK_THRESHOLD_DB = 3.0;
    private static final double MEDIUM_RISK_THRESHOLD_DB = 0.5;

    private final MemoryDataMart dataMart;

    public RainFadeService(MemoryDataMart dataMart) {
        this.dataMart = dataMart;
    }

    public List<RainFadeResponse.Prediction> getPredictionsForLocation(String location) {
        List<WeatherEvent> locationWeather = dataMart.getWeatherEvents().stream()
                .filter(w -> w.locationName().toLowerCase().contains(location.toLowerCase()))
                .limit(3)
                .toList();

        if (locationWeather.isEmpty()) {
            return List.of(); // Retornamos lista vacía si no hay datos
        }

        List<RainFadeResponse.SatelliteInfo> activeSatellites = getActiveSatellites();
        return buildPredictions(locationWeather, activeSatellites);
    }

    private List<RainFadeResponse.SatelliteInfo> getActiveSatellites() {
        return dataMart.getSatelliteEvents().stream()
                .collect(Collectors.toMap(SatelliteEvent::id, s -> s, (existing, replacement) -> replacement))
                .values().stream()
                .map(s -> new RainFadeResponse.SatelliteInfo(s.id(), s.latitude(), s.longitude()))
                .limit(200)
                .toList();
    }

    private List<RainFadeResponse.Prediction> buildPredictions(List<WeatherEvent> weatherEvents, List<RainFadeResponse.SatelliteInfo> satellites) {
        return weatherEvents.stream().map(weather -> {
            String risk = calculateRainFadeRisk(weather.description());
            RainFadeResponse.WeatherInfo weatherInfo = new RainFadeResponse.WeatherInfo(
                    weather.temperature(), weather.humidity(), 0, weather.description()
            );
            return new RainFadeResponse.Prediction("Predicción registrada", weatherInfo, satellites, risk);
        }).toList();
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