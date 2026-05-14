package org.ulpgc.starlink.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ulpgc.starlink.monitor.control.*;
import org.ulpgc.starlink.monitor.view.UIService;
import com.google.gson.JsonObject;

import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String eventStorePath = args.length > 0 ? args[0] : "eventstore";
        String dbPath = args.length > 1 ? args[1] : "datamart.db";

        SQLiteDataMart dataMart = new SQLiteDataMart(dbPath);
        dataMart.initTables();

        logger.info("🚀 Iniciando carga histórica (Limitada a los últimos 50k eventos para estabilidad)...");
        EventStoreReader reader = new EventStoreReader(eventStorePath);
        
        long startTime = System.currentTimeMillis();
        java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);
        int MAX_HISTORICAL_EVENTS = 50000;

        dataMart.beginTransaction();
        reader.readAndProcessEvents(event -> {
            if (count.get() < MAX_HISTORICAL_EVENTS) {
                dataMart.save(event);
                int current = count.incrementAndGet();
                if (current % 10000 == 0) {
                    logger.info("📊 Procesados {} eventos...", current);
                }
            }
        });
        dataMart.commitTransaction();

        long endTime = System.currentTimeMillis();
        logger.info("✅ Carga finalizada: {} eventos en {}s", count.get(), (endTime - startTime) / 1000);

        String brokerUrl = System.getenv("ACTIVEMQ_URL");
        if (brokerUrl == null) brokerUrl = "tcp://localhost:61616";

        ActiveMQSubscriber subscriber = new ActiveMQSubscriber(brokerUrl, dataMart);
        subscriber.start();

        // FLUJO REQUERIDO: UIService -> RainFadeController -> RainFadeService
        RainFadeService service = new RainFadeService(dataMart);
        RainFadeController controller = new RainFadeController(service);
        UIService uiService = new UIService(controller);

        uiService.start(7000);
    }
}
