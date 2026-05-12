package org.ulpgc.starlink.monitor;

import org.ulpgc.starlink.monitor.control.RainFadeController;
import org.ulpgc.starlink.monitor.infrastructure.broker.ActiveMQSubscriber;
import org.ulpgc.starlink.monitor.infrastructure.persistance.EventStoreReader;
import org.ulpgc.starlink.monitor.infrastructure.persistance.SQLiteDataMart;
import com.google.gson.JsonObject;
import org.ulpgc.starlink.monitor.services.RainFadeService;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Configuración de rutas (idealmente por program arguments)
        String eventStorePath = args.length > 0 ? args[0] : "eventstore";
        String dbPath = args.length > 1 ? args[1] : "datamart.db";

        // 2. Inicializar Repositorio (Datamart)
        SQLiteDataMart dataMart = new SQLiteDataMart(dbPath);
        dataMart.initTables(); // Crea las tablas si no existen

        // 3. CARGA DE HISTÓRICOS (Lo que pidió el profesor)
        System.out.println("Cargando datos históricos desde: " + eventStorePath);

        EventStoreReader reader = new EventStoreReader(eventStorePath);
        List<JsonObject> historicalEvents = reader.readAllEvents();

        for (JsonObject event : historicalEvents) {
            // Aquí procesas el JsonObject según sea Weather o SpaceX
            // y lo guardas en el datamart
            dataMart.save(event);
        }

        System.out.println(
                "Carga histórica completada. Eventos procesados: "
                        + historicalEvents.size()
        );

        // 4. Iniciar Suscriptor en Tiempo Real
        ActiveMQSubscriber subscriber =
                new ActiveMQSubscriber("tcp://localhost:61616", dataMart);

        subscriber.start();

        // 5. Iniciar Servidor API/Controlador (Javalin u otro)
        RainFadeService service = new RainFadeService(dataMart);
        RainFadeController controller = new RainFadeController(service);

        controller.start(7000);
    }
}