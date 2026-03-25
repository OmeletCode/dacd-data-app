package org.ulpgc.dacd;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SpaceXController {
    private final SpaceXSupplier supplier;
    private final SpaceXSerializer serializer;

    public SpaceXController(SpaceXSupplier supplier, SpaceXSerializer serializer) {
        this.supplier = supplier;
        this.serializer = serializer;
    }

    public void execute() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("\n[ " + java.time.LocalTime.now() + " ] Iniciando ciclo de captura...");
                List<Satellite> satellites = supplier.getSatellites();

                if (satellites != null && !satellites.isEmpty()) {
                    System.out.println("Guardando " + satellites.size() + " satélites en SQLite...");
                    for (Satellite sat : satellites) {
                        serializer.saveSatellite(sat);
                    }
                    System.out.println("¡Ciclo completado con éxito!");
                } else {
                    System.out.println("No se pudieron obtener satélites en este ciclo.");
                }
            }
        }, 0, 3600000); // Se ejecuta cada hora
    }
}