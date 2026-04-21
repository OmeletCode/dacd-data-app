package org.ulpgc.dacd.model;

import java.util.List;

// El objeto principal que devolveremos al HTML
public record RainFadeResponse(
        String location,
        String requestTime,
        List<Prediction> predictions
) {
    // El objeto interno para cada hora
    public record Prediction(
            String time,
            WeatherInfo weather,
            List<String> satellitesInView,
            String rainFadeRisk
    ) {}

    // El objeto interno para los datos del clima
    public record WeatherInfo(
            double temperature,
            int humidity,
            int clouds,
            String description
    ) {}
}