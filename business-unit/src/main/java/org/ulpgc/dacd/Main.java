package org.ulpgc.dacd;

import io.javalin.Javalin;
import org.ulpgc.dacd.broker.ActiveMQSubscriber;
import org.ulpgc.dacd.model.RainFadeResponse;
import org.ulpgc.dacd.model.SatelliteEvent;
import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.reader.EventStoreReader;
import org.ulpgc.dacd.repository.MemoryDataMart;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Business Unit (Datamart & API) ---");

        // 🧠 1. CREAMOS EL CEREBRO (El almacén en memoria)
        MemoryDataMart dataMart = new MemoryDataMart();

        // 📚 2. CARGAMOS EL PASADO (Lector Histórico)
        EventStoreReader reader = new EventStoreReader();
        String rutaClima = "C:/Users/elyon/Desktop/uni/segundo curso/segundo cuatri/DACD/dacd-data-app/eventstore/prediction.Weather/Weather-Feeder/20260420.events";
        List<WeatherEvent> climaHistorico = reader.readWeatherEvents(rutaClima);

        for (WeatherEvent event : climaHistorico) {
            dataMart.addWeather(event);
        }
        System.out.println("✅ Histórico cargado en memoria. Total clima: " + dataMart.getWeatherEvents().size());

        // 📡 3. ESCUCHAMOS EL PRESENTE (ActiveMQ)
        ActiveMQSubscriber subscriber = new ActiveMQSubscriber(dataMart);
        subscriber.start();

        // 🚀 4. ABRIMOS LA PUERTA AL FUTURO (Javalin API)
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(8080);

        // Endpoint para el Dashboard Web
        app.get("/api/rainfade/{isla}", ctx -> {
            String location = ctx.pathParam("isla");
            String locationLimpia = location.replace("-", " ");

            // 1. Buscamos el clima de esa isla en la memoria
            List<WeatherEvent> climaIsla = dataMart.getWeatherEvents().stream()
                    .filter(w -> w.location().equalsIgnoreCase(locationLimpia))
                    .limit(3)
                    .toList();

            if (climaIsla.isEmpty()) {
                ctx.status(404).result("No hay datos climáticos registrados para: " + locationLimpia);
                return;
            }

            // 2. Buscamos satélites ÚNICOS en memoria
            List<RainFadeResponse.SatelliteInfo> satelitesActivos = dataMart.getSatelliteEvents().stream()
                    // Agrupamos por ID y nos quedamos con la última posición conocida de cada uno
                    .collect(java.util.stream.Collectors.toMap(
                            SatelliteEvent::id,
                            s -> s,
                            (existente, reemplazo) -> reemplazo)
                    )
                    .values().stream()
                    .map(s -> new RainFadeResponse.SatelliteInfo(s.id(), s.lat(), s.lon()))
                    .limit(10)
                    .toList();

            // 3. Calculamos riesgo y empaquetamos predicciones
            List<RainFadeResponse.Prediction> predictions = climaIsla.stream().map(clima -> {

                // --- Lógica de Riesgo ---
                String riesgo = "LOW";
                String desc = clima.description().toLowerCase();
                if (desc.contains("rain") || desc.contains("drizzle")) {
                    riesgo = "HIGH";
                } else if (desc.contains("clouds") || desc.contains("mist")) {
                    riesgo = "MEDIUM";
                }

                // --- Objeto de Clima ---
                RainFadeResponse.WeatherInfo infoClima = new RainFadeResponse.WeatherInfo(
                        clima.temperature(),
                        clima.humidity(),
                        0, // Clouds opcional
                        clima.description()
                );

                return new RainFadeResponse.Prediction(
                        "Predicción registrada",
                        infoClima,
                        satelitesActivos,
                        riesgo
                );
            }).toList();

            // 4. Respuesta Final JSON
            RainFadeResponse response = new RainFadeResponse(
                    locationLimpia,
                    java.time.Instant.now().toString(),
                    predictions
            );

            ctx.json(response);
        });
    }
}