package org.ulpgc.dacd.business_unit;

import org.ulpgc.dacd.business_unit.broker.ActiveMQSubscriber;
import org.ulpgc.dacd.business_unit.reader.EventStoreReader;
import org.ulpgc.dacd.business_unit.repository.MemoryDataMart;
import org.ulpgc.dacd.business_unit.model.WeatherEvent;
import org.ulpgc.dacd.business_unit.model.SatelliteEvent; // No olvides el import

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Business Unit ---");

        // Ahora pedimos 2 argumentos
        if (args.length < 2) {
            System.err.println("❌ ERROR: Debes proporcionar DOS rutas: [Ruta Clima] [Ruta Satélites]");
            System.exit(1);
        }

        String historicalWeatherPath = args[0];
        String historicalSatellitePath = args[1];

        MemoryDataMart dataMart = new MemoryDataMart();

        // Cargamos TODO el histórico antes de levantar la web
        loadHistoricalWeather(dataMart, historicalWeatherPath);
        loadHistoricalSatellites(dataMart, historicalSatellitePath);

        RainFadeService service = new RainFadeService(dataMart);
        RainFadeController apiController = new RainFadeController(service);

        startActiveMQSubscriber(dataMart);
        apiController.start(8080);
    }

    private static void loadHistoricalWeather(MemoryDataMart dataMart, String filePath) {
        EventStoreReader reader = new EventStoreReader();
        List<WeatherEvent> historicalWeather = reader.readWeatherEvents(filePath);
        if (historicalWeather != null && !historicalWeather.isEmpty()) {
            historicalWeather.forEach(dataMart::addWeather);
            System.out.println("✅ Histórico CLIMA cargado: " + historicalWeather.size() + " registros.");
        }
    }

    // --- NUEVO MÉTODO DE ARRANQUE ---
    private static void loadHistoricalSatellites(MemoryDataMart dataMart, String filePath) {
        EventStoreReader reader = new EventStoreReader();
        List<SatelliteEvent> historicalSatellites = reader.readSatelliteEvents(filePath);
        if (historicalSatellites != null && !historicalSatellites.isEmpty()) {
            historicalSatellites.forEach(dataMart::addSatellite);
            System.out.println("✅ Histórico SATÉLITES cargado: " + historicalSatellites.size() + " registros.");
        }
    }

    private static void startActiveMQSubscriber(MemoryDataMart dataMart) {
        ActiveMQSubscriber subscriber = new ActiveMQSubscriber(dataMart);
        subscriber.start();
    }
}