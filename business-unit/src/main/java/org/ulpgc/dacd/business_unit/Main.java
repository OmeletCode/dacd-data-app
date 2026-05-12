package org.ulpgc.dacd.business_unit;

import org.ulpgc.dacd.business_unit.broker.ActiveMQSubscriber;
import org.ulpgc.dacd.business_unit.reader.EventStoreReader;
import org.ulpgc.dacd.business_unit.model.WeatherEvent;
import org.ulpgc.dacd.business_unit.model.SatelliteEvent;
import org.ulpgc.dacd.business_unit.repository.SQLiteDataMart;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Business Unit ---");

        if (args.length < 2) {
            System.err.println("❌ ERROR: Debes proporcionar DOS rutas: [Ruta Clima] [Ruta Satélites]");
            System.exit(1);
        }

        String historicalWeatherPath = args[0];
        String historicalSatellitePath = args[1];

        String dbPath = "datamart.db";
        SQLiteDataMart dataMart = new SQLiteDataMart(dbPath);

        loadHistoricalWeather(dataMart, historicalWeatherPath);
        loadHistoricalSatellites(dataMart, historicalSatellitePath);

        RainFadeService service = new RainFadeService(dataMart);
        RainFadeController apiController = new RainFadeController(service);

        startActiveMQSubscriber(dataMart);
        apiController.start(8080);
    }

    private static void loadHistoricalWeather(SQLiteDataMart dataMart, String filePath) {
        EventStoreReader reader = new EventStoreReader();
        List<WeatherEvent> historicalWeather = reader.readWeatherEvents(filePath);
        if (historicalWeather != null && !historicalWeather.isEmpty()) {
            historicalWeather.forEach(dataMart::addWeather);
            System.out.println("✅ Histórico CLIMA cargado: " + historicalWeather.size() + " registros.");
        }
    }

    private static void loadHistoricalSatellites(SQLiteDataMart dataMart, String filePath) {
        EventStoreReader reader = new EventStoreReader();
        List<SatelliteEvent> historicalSatellites = reader.readSatelliteEvents(filePath);
        if (historicalSatellites != null && !historicalSatellites.isEmpty()) {
            dataMart.addSatellites(historicalSatellites);
            System.out.println("✅ Histórico SATÉLITES cargado: " + historicalSatellites.size() + " registros.");
        }
    }

    private static void startActiveMQSubscriber(SQLiteDataMart dataMart) {
        ActiveMQSubscriber subscriber = new ActiveMQSubscriber(dataMart);
        subscriber.start();
    }
}