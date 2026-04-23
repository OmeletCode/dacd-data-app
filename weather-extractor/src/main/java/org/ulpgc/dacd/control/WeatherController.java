package org.ulpgc.dacd.control;

import java.util.Timer;
import java.util.TimerTask;
import org.ulpgc.dacd.model.WeatherEvent;
import org.ulpgc.dacd.model.ActiveMQMessageSender;
import org.ulpgc.dacd.model.GsonEventSerializer;

public class WeatherController {
    private final WeatherSupplier supplier;

    public WeatherController(WeatherSupplier supplier) {
        this.supplier = supplier;
    }

    public void execute() {
        Timer timer = new Timer();
        GsonEventSerializer jsonSerializer = new GsonEventSerializer();

        // --- PUNTO 7: Instanciamos el cartero para el canal del clima ---
        ActiveMQMessageSender sender = new ActiveMQMessageSender("prediction.Weather");

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("\n[ " + java.time.LocalTime.now() + " ] Obteniendo datos meteorológicos...");
                try {
                    WeatherEvent weatherEvent = supplier.get(28.12, -15.43);

                    if (weatherEvent != null) {
                        // 1. Pasamos a texto (Punto 6)
                        String json = jsonSerializer.serialize(weatherEvent);

                        // 2. Lo enviamos por internet al Broker (Punto 7)
                        sender.sendMessages(java.util.List.of(json));
                    }
                } catch (Exception e) {
                    System.err.println("Error al capturar el clima: " + e.getMessage());
                }
            }
        }, 0, 15 * 60 * 1000); // Se ejecuta cada 15 minutos
    }
}