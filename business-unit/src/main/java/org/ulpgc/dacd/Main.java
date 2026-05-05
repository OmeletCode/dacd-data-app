package org.ulpgc.dacd;

import io.javalin.Javalin;
import org.ulpgc.dacd.broker.ActiveMQSubscriber;
import org.ulpgc.dacd.model.RainFadeResponse;
import org.ulpgc.dacd.reader.EventStoreReader;
import org.ulpgc.dacd.repository.MemoryDataMart;
import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.model.SatelliteEvent;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final String HISTORICAL_WEATHER_PATH = "C:/Users/elyon/Desktop/uni/segundo curso/segundo cuatri/DACD/dacd-data-app/eventstore/prediction.Weather/Weather-Feeder/20260420.events";

    private static final double KU_BAND_FACTOR_A = 0.0188;
    private static final double KU_BAND_FACTOR_B = 1.15;
    private static final double HIGH_RISK_THRESHOLD_DB = 3.0;
    private static final double MEDIUM_RISK_THRESHOLD_DB = 0.5;

    public static void main(String[] args) {
        System.out.println("--- Iniciando Business Unit (Datamart & API) ---");

        MemoryDataMart dataMart = new MemoryDataMart();

        loadHistoricalWeather(dataMart);
        startActiveMQSubscriber(dataMart);
        startApiServer(dataMart);
    }

    private static void loadHistoricalWeather(MemoryDataMart dataMart) {
        EventStoreReader reader = new EventStoreReader();
        List<WeatherEvent> historicalWeather = reader.readWeatherEvents(HISTORICAL_WEATHER_PATH);
        historicalWeather.forEach(dataMart::addWeather);

        System.out.println("✅ Histórico cargado. Total clima: " + dataMart.getWeatherEvents().size());
    }

    private static void startActiveMQSubscriber(MemoryDataMart dataMart) {
        ActiveMQSubscriber subscriber = new ActiveMQSubscriber(dataMart);
        subscriber.start();
    }

    private static void startApiServer(MemoryDataMart dataMart) {
        Javalin app = Javalin.create(config -> config.staticFiles.add("/public")).start(8080);

        app.get("/api/rainfade/{isla}", ctx -> {
            String location = ctx.pathParam("isla").replace("-", " ");

            List<WeatherEvent> locationWeather = getLocationWeather(dataMart, location);
            if (locationWeather.isEmpty()) {
                ctx.status(404).result("No hay datos climáticos registrados para: " + location);
                return;
            }

            List<RainFadeResponse.SatelliteInfo> activeSatellites = getActiveSatellites(dataMart);
            List<RainFadeResponse.Prediction> predictions = buildPredictions(locationWeather, activeSatellites);

            RainFadeResponse response = new RainFadeResponse(location, Instant.now().toString(), predictions);
            ctx.json(response);
        });
    }

    private static List<WeatherEvent> getLocationWeather(MemoryDataMart dataMart, String location) {
        return dataMart.getWeatherEvents().stream()
                .filter(w -> w.locationName().toLowerCase().contains(location.toLowerCase()))
                .limit(3)
                .toList();
    }

    private static List<RainFadeResponse.SatelliteInfo> getActiveSatellites(MemoryDataMart dataMart) {
        return dataMart.getSatelliteEvents().stream()
                .collect(Collectors.toMap(SatelliteEvent::id, s -> s, (existing, replacement) -> replacement))
                .values().stream()
                .map(s -> new RainFadeResponse.SatelliteInfo(s.id(), s.latitude(), s.longitude()))
                .limit(200)
                .toList();
    }

    private static List<RainFadeResponse.Prediction> buildPredictions(List<WeatherEvent> weatherEvents, List<RainFadeResponse.SatelliteInfo> satellites) {
        return weatherEvents.stream().map(weather -> {
            String risk = calculateRainFadeRisk(weather.description());
            RainFadeResponse.WeatherInfo weatherInfo = new RainFadeResponse.WeatherInfo(
                    weather.temperature(), weather.humidity(), 0, weather.description()
            );
            return new RainFadeResponse.Prediction("Predicción registrada", weatherInfo, satellites, risk);
        }).toList();
    }

    private static String calculateRainFadeRisk(String weatherDescription) {
        double rainRate = estimateRainRateMmPerHour(weatherDescription);
        double attenuationDb = KU_BAND_FACTOR_A * Math.pow(rainRate, KU_BAND_FACTOR_B);

        if (attenuationDb > HIGH_RISK_THRESHOLD_DB) return "HIGH";
        if (attenuationDb > MEDIUM_RISK_THRESHOLD_DB) return "MEDIUM";
        if (weatherDescription != null && weatherDescription.toLowerCase().contains("clouds")) return "MEDIUM";

        return "LOW";
    }

    private static double estimateRainRateMmPerHour(String description) {
        if (description == null) return 0.0;
        String desc = description.toLowerCase();

        if (desc.contains("heavy") || desc.contains("extreme") || desc.contains("thunderstorm")) return 25.0;
        if (desc.contains("moderate") || desc.equals("rain")) return 10.0;
        if (desc.contains("light") || desc.contains("drizzle")) return 2.5;

        return 0.0;
    }
}