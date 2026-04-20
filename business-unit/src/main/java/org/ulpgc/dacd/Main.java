package org.ulpgc.dacd;

import io.javalin.Javalin;
import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.reader.EventStoreReader;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Business Unit (Datamart & API) ---");

        // --- PRUEBA DEL LECTOR HISTÓRICO ---
        EventStoreReader reader = new EventStoreReader();

        // IMPORTANTE: Cambia esta ruta a donde esté realmente tu archivo .events
        // Puedes hacer clic derecho en tu archivo .events -> Copy Path/Reference -> Absolute Path
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

        // --- LEVANTAR EL SERVIDOR JAVALIN ---
        Javalin app = Javalin.create().start(8080);

        app.get("/", ctx -> {
            ctx.result("🚀 ¡API del Monitor Predictivo de Rain Fade funcionando correctamente!");
        });
    }
}