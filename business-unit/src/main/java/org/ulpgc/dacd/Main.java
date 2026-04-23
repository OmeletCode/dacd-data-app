package org.ulpgc.dacd;

import io.javalin.Javalin;
import org.ulpgc.dacd.broker.ActiveMQSubscriber;
import org.ulpgc.dacd.model.RainFadeResponse;
import org.ulpgc.dacd.reader.EventStoreReader;
import org.ulpgc.dacd.repository.MemoryDataMart;

import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.model.SatelliteEvent;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Business Unit (Datamart & API) ---");

        // 🧠 1. CREAMOS EL CEREBRO (El almacén en memoria)
        MemoryDataMart dataMart = new MemoryDataMart();

        // 📚 2. CARGAMOS EL PASADO (Lector Histórico)
        EventStoreReader reader = new EventStoreReader();
        // Nota: Asegúrate de que esta ruta sea correcta para el día de hoy
        String rutaClima = "C:/Users/elyon/Desktop/uni/segundo curso/segundo cuatri/DACD/dacd-data-app/eventstore/prediction.Weather/Weather-Feeder/20260420.events";
        List<WeatherEvent> climaHistorico = reader.readWeatherEvents(rutaClima);

        for (WeatherEvent event : climaHistorico) {
            dataMart.addWeather(event);
        }
        System.out.println("✅ Histórico cargado en memoria. Total clima: " + dataMart.getWeatherEvents().size());

        dataMart.getWeatherEvents().stream()
                .map(w -> w.location())
                .distinct()
                .forEach(loc -> System.out.println("📍 Localización disponible en memoria: " + loc));

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
                    .filter(w -> w.location().toLowerCase().contains(locationLimpia.toLowerCase()))
                    .limit(3)
                    .toList();

            if (climaIsla.isEmpty()) {
                ctx.status(404).result("No hay datos climáticos registrados para: " + locationLimpia);
                return;
            }

            // 2. Buscamos satélites ÚNICOS en memoria
            List<RainFadeResponse.SatelliteInfo> satelitesActivos = dataMart.getSatelliteEvents().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            SatelliteEvent::id,
                            s -> s,
                            (existente, reemplazo) -> reemplazo)
                    )
                    .values().stream()
                    .map(s -> new RainFadeResponse.SatelliteInfo(s.id(), s.lat(), s.lon()))
                    .limit(200)
                    .toList();

            // 3. Calculamos riesgo y empaquetamos predicciones
            List<RainFadeResponse.Prediction> predictions = climaIsla.stream().map(clima -> {

                // --- Lógica de Riesgo (Modelo Físico ITU-R para Banda Ku de Starlink) ---
                double rainRate = estimarLluvia(clima.description());

                // Constantes aproximadas de atenuación para ~12 GHz (Descarga Starlink)
                double a = 0.0188;
                double b = 1.15;

                // Cálculo: A = a * R^b
                double atenuacionDB = a * Math.pow(rainRate, b);

                String riesgo = "LOW";
                if (atenuacionDB > 3.0) {
                    // Si perdemos más de 3 decibelios, la señal cae drásticamente
                    riesgo = "HIGH";
                } else if (atenuacionDB > 0.5) {
                    // Si perdemos algo de señal pero es manejable
                    riesgo = "MEDIUM";
                } else if (clima.description().toLowerCase().contains("clouds")) {
                    // Nubes densas sin lluvia fuerte = riesgo medio preventivo
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

    // Función para estimar la tasa de lluvia (R) en mm/h según el texto
    private static double estimarLluvia(String descripcion) {
        if (descripcion == null) return 0.0;
        String desc = descripcion.toLowerCase();

        if (desc.contains("heavy") || desc.contains("extreme") || desc.contains("thunderstorm")) {
            return 25.0; // Lluvia fuerte (mm/h)
        } else if (desc.contains("moderate") || desc.equals("rain")) {
            return 10.0; // Lluvia moderada
        } else if (desc.contains("light") || desc.contains("drizzle")) {
            return 2.5;  // Llovizna
        }
        return 0.0; // Despejado o nublado sin precipitación
    }
}