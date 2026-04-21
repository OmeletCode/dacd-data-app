package org.ulpgc.dacd.model;

import java.util.List;

public record RainFadeResponse(
        String location,
        String requestTime,
        List<Prediction> predictions
) {
    // 🛰️ NUEVO: Record para enviar la posición del satélite
    public record SatelliteInfo(String id, double lat, double lon) {}

    public record Prediction(
            String time,
            WeatherInfo weather,
            List<SatelliteInfo> satellitesInView, // 👈 Cambiado de List<String> a List<SatelliteInfo>
            String rainFadeRisk
    ) {}

    public record WeatherInfo(
            double temperature,
            int humidity,
            int clouds,
            String description
    ) {}
}