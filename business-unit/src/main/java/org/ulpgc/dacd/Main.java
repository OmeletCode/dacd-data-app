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

        // Volcamos el archivo a la memoria
        for (WeatherEvent event : climaHistorico) {
            dataMart.addWeather(event);
        }
        System.out.println("✅ Histórico cargado en memoria. Total clima: " + dataMart.getWeatherEvents().size());

        // 📡 3. ESCUCHAMOS EL PRESENTE (ActiveMQ)
        ActiveMQSubscriber subscriber = new ActiveMQSubscriber(dataMart);
        subscriber.start();

        // 🚀 4. ABRIMOS LA PUERTA AL FUTURO (Javalin API)
        // --- LEVANTAR EL SERVIDOR JAVALIN ---
        Javalin app = Javalin.create(config -> {
            // Le decimos a Javalin que aloje nuestra página web estática
            config.staticFiles.add("/public");
        }).start(8080);


        // Tu endpoint para la web
        // Ruta 2: El Endpoint DEFINITIVO con Lógica de Negocio
        app.get("/api/rainfade/{isla}", ctx -> {
            String location = ctx.pathParam("isla");
            // Truco: Si la URL tiene "Las-Palmas", lo cambiamos a "Las Palmas" para buscarlo bien
            String locationLimpia = location.replace("-", " ");

            // 1. Buscamos el clima de esa isla en la memoria (cogemos las 3 predicciones más recientes)
            List<WeatherEvent> climaIsla = dataMart.getWeatherEvents().stream()
                    .filter(w -> w.location().equalsIgnoreCase(locationLimpia))
                    .limit(3)
                    .toList();

            // Si nadie ha pedido datos de esa isla, devolvemos un aviso 404
            if (climaIsla.isEmpty()) {
                ctx.status(404).result("No hay datos climáticos registrados para: " + locationLimpia);
                return; // Cortamos la ejecución aquí
            }

            // 2. Buscamos los satélites que tenemos en memoria (cogemos 5 nombres sin repetir)
            List<String> satelitesActivos = dataMart.getSatelliteEvents().stream()
                    .map(SatelliteEvent::id)
                    .distinct()
                    .limit(5)
                    .toList();

            // 3. Cruzamos los datos y calculamos el riesgo de desconexión
            List<RainFadeResponse.Prediction> predictions = climaIsla.stream().map(clima -> {

                // --- LÓGICA DE NEGOCIO ---
                String riesgo = "LOW";
                // Asumimos que tu WeatherEvent tiene description() y clouds() (cámbialos si se llaman distinto)
                if (clima.description().toLowerCase().contains("rain") /* || clima.clouds() > 80 */) {
                    riesgo = "HIGH";
                } else if (clima.description().toLowerCase().contains("clouds")) {
                    riesgo = "MEDIUM";
                }

                RainFadeResponse.WeatherInfo info = new RainFadeResponse.WeatherInfo(
                        clima.temperature(),
                        0, // Ponemos 0 temporalmente si tu WeatherEvent no tiene la humedad guardada
                        0, // Ponemos 0 temporalmente si no tienes las nubes guardadas
                        clima.description()
                );

                return new RainFadeResponse.Prediction(
                        "Predicción registrada", // Aquí idealmente iría clima.instant() o la fecha
                        info,
                        satelitesActivos,
                        riesgo
                );
            }).toList();

            // 4. Empaquetamos y enviamos el JSON final
            RainFadeResponse response = new RainFadeResponse(
                    locationLimpia,
                    java.time.Instant.now().toString(),
                    predictions
            );

            ctx.json(response);
        });
    }
}