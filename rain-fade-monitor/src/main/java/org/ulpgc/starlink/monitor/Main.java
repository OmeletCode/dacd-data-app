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

        java.io.File eventStoreDir = new java.io.File(eventStorePath);
        if (!eventStoreDir.exists() || !eventStoreDir.isDirectory()) {
            logger.error("❌ ERROR: El directorio del EventStore no existe: {}. Verifica la ruta.", eventStoreDir.getAbsolutePath());
        }

        SQLiteDataMart dataMart = new SQLiteDataMart(dbPath);
        dataMart.initTables();

        logger.info("🚀 Iniciando carga histórica inteligente...");
        EventStoreReader reader = new EventStoreReader(eventStorePath);
        
        long startTime = System.currentTimeMillis();
        java.util.concurrent.atomic.AtomicInteger weatherCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger spacexCount = new java.util.concurrent.atomic.AtomicInteger(0);
        
        int MAX_SPACEX_EVENTS = 300000; // Suficiente para varias ráfagas completas de la constelación

        dataMart.beginTransaction();
        reader.readAndProcessEvents(event -> {
            String ss = event.has("ss") ? event.get("ss").getAsString().toLowerCase() : "";
            
            if (ss.contains("weather")) {
                dataMart.save(event);
                weatherCount.incrementAndGet();
            } else if (ss.contains("spacex") || ss.contains("satellite")) {
                if (spacexCount.get() < MAX_SPACEX_EVENTS) {
                    dataMart.save(event);
                    spacexCount.incrementAndGet();
                }
            }

            int total = weatherCount.get() + spacexCount.get();
            if (total > 0 && total % 20000 == 0) {
                logger.info("📊 Procesados {} eventos (Clima: {}, Satélites: {})...", 
                        total, weatherCount.get(), spacexCount.get());
            }
        });
        dataMart.commitTransaction();

        long endTime = System.currentTimeMillis();
        logger.info("✅ Carga finalizada: {} eventos ({} clima / {} satélites) en {}s", 
                weatherCount.get() + spacexCount.get(), weatherCount.get(), spacexCount.get(), (endTime - startTime) / 1000);

        String brokerUrl = System.getenv("ACTIVEMQ_URL");
        if (brokerUrl == null) brokerUrl = "tcp://localhost:61616";

        ActiveMQSubscriber subscriber = new ActiveMQSubscriber(brokerUrl, dataMart);
        subscriber.start();

        // Architectural Flow: UIService -> RainFadeController -> RainFadeService
        RainFadeService service = new RainFadeService(dataMart);
        RainFadeController controller = new RainFadeController(service);
        UIService uiService = new UIService(controller);

        uiService.start(7000);
    }
}
