package org.ulpgc.starlink.monitor.view;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ulpgc.starlink.monitor.control.RainFadeController;

public record UIService(RainFadeController controller) {
    private static final Logger logger = LoggerFactory.getLogger(UIService.class);
    private static final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    
    private static final Counter wsConnections = Counter.builder("websocket_connections_total")
            .description("Total number of WebSocket connections")
            .register(registry);

    public void start(int port) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        }).start(port);

        logger.info("🚀 Servidor UIService iniciado en el puerto {}", port);

        // Prometheus Metrics Endpoint
        app.get("/metrics", ctx -> {
            ctx.contentType("text/plain").result(registry.scrape());
        });

        // Health Check Endpoint
        app.get("/api/v1/health", ctx -> {
            ctx.status(200).result("UP");
        });

        // REST Endpoint for Predictions
        // Example: /api/v1/predictions/LasPalmas?hours=0
        app.get("/api/v1/predictions/{location}", ctx -> {
            String location = ctx.pathParam("location");
            int hours = ctx.queryParamAsClass("hours", Integer.class).getOrDefault(0);
            
            var data = controller.getRainFadeData(location, hours);
            if (data.predictions().isEmpty()) {
                ctx.status(404).result("Location not found or no data available");
            } else {
                ctx.json(data);
            }
        });

        app.ws("/ws/v1/rainfade", ws -> {
            ws.onConnect(ctx -> {
                wsConnections.increment();
                logger.info("🔌 WebSocket v1: Nueva conexión abierta");
            });
            ws.onMessage(ctx -> {
                String island = ctx.message();
                controller.addSession(ctx, island);
                logger.info("🔌 WebSocket: Recibida solicitud para isla: {}", island);
            });
            ws.onClose(ctx -> {
                controller.removeSession(ctx);
                logger.info("🔌 WebSocket: Conexión cerrada");
            });
        });

        // The controller handles periodic data broadcasting
        controller.startBroadcaster();
    }
}
