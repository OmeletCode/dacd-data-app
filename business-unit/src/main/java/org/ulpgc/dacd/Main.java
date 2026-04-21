package org.ulpgc.dacd;

import io.javalin.Javalin;
import org.ulpgc.dacd.broker.ActiveMQSubscriber;
import org.ulpgc.dacd.model.RainFadeResponse;
import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.reader.EventStoreReader;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Business Unit (Datamart & API) ---");

        // --- PRUEBA DEL LECTOR HISTÓRICO ---
        EventStoreReader reader = new EventStoreReader();

        // RUTA DE TU ARCHIVO
        String rutaClima = "C:/Users/elyon/Desktop/uni/segundo curso/segundo cuatri/DACD/dacd-data-app/eventstore/prediction.Weather/Weather-Feeder/20260420.events";

        List<WeatherEvent> climaHistorico = reader.readWeatherEvents(rutaClima);

        System.out.println("✅ Archivo leído correctamente. Eventos cargados: " + climaHistorico.size());
        if (!climaHistorico.isEmpty()) {
            System.out.println("🔍 Ejemplo del primer evento transformado a Java:");
            System.out.println("   📍 Isla: " + climaHistorico.get(0).location());
            System.out.println("   🌡️ Temp: " + climaHistorico.get(0).temperature() + " ºC");
            System.out.println("   ☁️ Clima: " + climaHistorico.get(0).description());
        }
        System.out.println("--------------------------------------------------");

        // --- SUSCRIPTOR EN TIEMPO REAL ---
        ActiveMQSubscriber subscriber = new ActiveMQSubscriber();
        subscriber.start();

        // --- LEVANTAR EL SERVIDOR JAVALIN ---
        Javalin app = Javalin.create().start(8080);

        // Ruta 1: La de bienvenida (Raíz)
        app.get("/", ctx -> {
            ctx.result("🚀 ¡API del Monitor Predictivo de Rain Fade funcionando correctamente!");
        });

        // Ruta 2: El Endpoint para tu página web
        app.get("/api/rainfade/{isla}", ctx -> {
            // Recogemos el nombre de la isla que el usuario ponga en la URL
            String location = ctx.pathParam("isla");

            // 1. Creamos datos falsos (MOCK) temporalmente
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

            // 2. Javalin convierte tu Record a JSON y lo devuelve al navegador
            ctx.json(response);
        });
    }
}