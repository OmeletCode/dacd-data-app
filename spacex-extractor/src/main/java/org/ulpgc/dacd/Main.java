package org.ulpgc.dacd;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando el Extractor Automático de Starlink (Clean Code) ---");

        // 1. Instanciamos a nuestros "especialistas"
        SpaceXApiClient apiClient = new SpaceXApiClient();
        SpaceXParser parser = new SpaceXParser();
        DatabaseManager dbManager = new DatabaseManager();

        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("\n[ " + java.time.LocalTime.now() + " ] Iniciando ciclo de captura...");
                try {
                    // Paso A: El mensajero trae el texto de internet
                    String rawJson = apiClient.getRawData();

                    // Paso B: El traductor convierte el texto en objetos Java
                    List<Satellite> satellites = parser.parse(rawJson);

                    // Paso C: El archivero guarda los objetos en la base de datos
                    System.out.println("Guardando " + satellites.size() + " satélites en SQLite...");
                    for (Satellite sat : satellites) {
                        dbManager.saveSatellite(sat.name(), sat.rawJson());
                    }

                    System.out.println("¡Ciclo completado con éxito!");

                } catch (Exception e) {
                    System.out.println("Ocurrió un error en este ciclo: " + e.getMessage());
                }
            }
        };

        // Ejecutar cada hora (3600000 milisegundos).
        // Si quieres probarlo rápido, pon 5000 (5 segundos).
        timer.scheduleAtFixedRate(task, 0, 3600000);
    }
}