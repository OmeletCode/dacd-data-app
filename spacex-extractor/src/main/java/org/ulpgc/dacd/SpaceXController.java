package org.ulpgc.dacd;

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
                    for (SpaceXEvent evento : eventos) {
                        String json = jsonSerializer.serialize(evento);

                        sender.sendMessage(json);
                    }
                    System.out.println("-> Enviados " + eventos.size() + " satélites a ActiveMQ.");
                } else {
                    System.out.println("No se pudieron obtener satélites en este ciclo.");
                }
            }
        }, 0, 3600000); // Se ejecuta cada hora
    }
}