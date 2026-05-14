package org.ulpgc.starlink.monitor.control;

import io.javalin.websocket.WsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ulpgc.starlink.monitor.model.RainFadeResponse;
import com.google.gson.Gson;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RainFadeController {
    private static final Logger logger = LoggerFactory.getLogger(RainFadeController.class);
    private final RainFadeService service;
    private final Map<WsContext, SessionState> activeSessions = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    private record SessionState(String location, int historyHours) {}

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService broadcastPool = Executors.newFixedThreadPool(10);

    public RainFadeController(RainFadeService service) {
        this.service = service;
    }

    public RainFadeResponse getRainFadeData(String location, int historyHours) {
        String timestamp = Instant.now().minusSeconds(historyHours * 3600L).toString();
        return new RainFadeResponse(
                location,
                timestamp,
                service.getPredictionsForLocationAt(location, timestamp),
                service.getServiceHealth()
        );
    }

    public void addSession(WsContext ctx, String message) {
        // Formato: "IslandName" o "IslandName:Hours"
        String[] parts = message.split(":");
        String island = parts[0];
        int hours = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        activeSessions.put(ctx, new SessionState(island, hours));
        
        // Enviar respuesta inmediata al cambiar de forma asíncrona
        broadcastPool.submit(() -> {
            if (ctx.session.isOpen()) {
                ctx.send(gson.toJson(getRainFadeData(island, hours)));
            }
        });
    }

    public void removeSession(WsContext ctx) {
        activeSessions.remove(ctx);
    }

    public void startBroadcaster() {
        scheduler.scheduleAtFixedRate(() -> {
            activeSessions.forEach((ctx, state) -> {
                broadcastPool.submit(() -> {
                    try {
                        // Solo enviamos actualizaciones automáticas si estamos en modo "Tiempo Real" (0 horas)
                        if (ctx.session.isOpen() && state.historyHours == 0) {
                            RainFadeResponse data = getRainFadeData(state.location, 0);
                            ctx.send(gson.toJson(data));
                        } else if (!ctx.session.isOpen()) {
                            removeSession(ctx);
                        }
                    } catch (Exception e) {
                        logger.error("Error en broadcast WebSocket: {}", e.getMessage());
                        removeSession(ctx);
                    }
                });
            });
        }, 0, 2, TimeUnit.SECONDS);
    }
}
