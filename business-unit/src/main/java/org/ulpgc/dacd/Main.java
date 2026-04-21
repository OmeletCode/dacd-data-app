package org.ulpgc.dacd;

import io.javalin.Javalin;
import org.ulpgc.dacd.broker.ActiveMQSubscriber;
import org.ulpgc.dacd.model.RainFadeResponse;
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
        Javalin app = Javalin.create().start(8080);

        app.get("/", ctx -> {
            ctx.result("🚀 ¡API del Monitor Predictivo funcionando! Datos en memoria: " +
                    dataMart.getWeatherEvents().size() + " predicciones climáticas.");
        });

        // Tu endpoint para la web
        app.get("/api/rainfade/{isla}", ctx -> {
            String location = ctx.pathParam("isla");

            RainFadeResponse.WeatherInfo weatherInfo = new RainFadeResponse.WeatherInfo(22.5, 60, 20, "few clouds");
            RainFadeResponse.Prediction prediction = new RainFadeResponse.Prediction(
                    "15:00:00",
                    weatherInfo,
                    List.of("STARLINK-30", "STARLINK-74"),
                    "LOW"
            );
            RainFadeResponse response = new RainFadeResponse(
                    location,
                    "2026-04-21T12:00:00Z",
                    List.of(prediction)
            );
            ctx.json(response);
        });
    }
}