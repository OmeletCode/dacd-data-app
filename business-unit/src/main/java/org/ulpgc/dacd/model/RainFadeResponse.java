package org.ulpgc.dacd.model;

import java.util.List;

public record RainFadeResponse(
        String location,
        String requestTime,
        List<Prediction> predictions
) {
    public record SatelliteInfo(String id, double lat, double lon) {}

    public record Prediction(
            String time,
            WeatherInfo weather,
            List<SatelliteInfo> satellitesInView,
            String rainFadeRisk
    ) {}

    public record WeatherInfo(
            double temperature,
            int humidity,
            int clouds,
            String description
    ) {}
}