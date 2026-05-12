package org.ulpgc.starlink.monitor.model;

import java.util.List;

public record RainFadeResponse(
        String location,
        String timestamp,
        List<Prediction> predictions
) {
    public record Prediction(
            String info,
            WeatherInfo weather,
            List<SatelliteInfo> satellites,
            String risk,
            List<Double> tempHistory // <-- ¡Esta es la clave!
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
            double longitude
    ) {}
}