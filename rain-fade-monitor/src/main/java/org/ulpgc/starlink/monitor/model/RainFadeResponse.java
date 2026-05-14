package org.ulpgc.starlink.monitor.model;

import java.util.List;
import java.util.Map;

public record RainFadeResponse(
        String location,
        String timestamp,
        List<Prediction> predictions,
        Map<String, String> healthStatus
) {
    public record Prediction(
            String info,
            WeatherInfo weather,
            List<SatelliteInfo> satellites,
            String risk,
            List<Double> tempHistory,
            double maxAttenuationDb
    ) {}

    public record WeatherInfo(
            double temperature,
            int humidity,
            double windSpeed,
            String description
    ) {}

    public record SatelliteInfo(
            String id,
            double latitude,
            double longitude,
            double attenuationDb,
            double elevation
    ) {}
}
