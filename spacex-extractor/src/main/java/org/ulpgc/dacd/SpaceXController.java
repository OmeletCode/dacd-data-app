package org.ulpgc.dacd;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SpaceXController {
    private final SpaceXSupplier supplier;

    public SpaceXController(SpaceXSupplier supplier) {
        this.supplier = supplier;
    }

    public void execute() {
        Timer timer = new Timer();
        GsonEventSerializer jsonSerializer = new GsonEventSerializer();
        ActiveMQMessageSender sender = new ActiveMQMessageSender("sensor.SpaceX");

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("\n[ " + java.time.LocalTime.now() + " ] Iniciando captura de SpaceX...");
                List<SpaceXEvent> eventos = supplier.getSatellites();

                if (eventos != null && !eventos.isEmpty()) {
                    List<String> jsonEvents = new ArrayList<>();

                    // Convertimos todos los eventos a JSON y los guardamos en la lista
                    for (SpaceXEvent evento : eventos) {
                        jsonEvents.add(jsonSerializer.serialize(evento));
                    }

                    // Enviamos la lista completa de golpe
                    sender.sendMessages(jsonEvents);

                } else {
                    System.out.println("No se pudieron obtener satélites en este ciclo.");
                }
            }
        }, 0, 3600000);
    }
}