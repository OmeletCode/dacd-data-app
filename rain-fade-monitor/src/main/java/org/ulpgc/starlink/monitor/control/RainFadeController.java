package org.ulpgc.starlink.monitor.control;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import org.ulpgc.starlink.monitor.services.RainFadeService;
import org.ulpgc.starlink.monitor.model.RainFadeResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RainFadeController {
    private final RainFadeService service;
    private final Gson gson = new Gson();
    private Javalin app;

    // Aquí guardamos quién está conectado y qué isla está mirando
    private final Map<WsContext, String> activeSessions = new ConcurrentHashMap<>();

    public RainFadeController(RainFadeService service) {
        this.service = service;
    }

    public void start(int port) {
        app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(port);

        // Dejamos tu antigua API REST por si acaso (buenas prácticas)
        app.get("/api/rainfade/{isla}", ctx -> {
            String location = ctx.pathParam("isla");
            ctx.result(gson.toJson(getRainFadeData(location))).contentType("application/json");
        });

        // 🚀 NUEVA API WEBSOCKET
        app.ws("/ws/rainfade", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("🔌 Nuevo cliente conectado: " + ctx.sessionId());
            });

            ws.onMessage(ctx -> {
                try {
                    // Cuando el cliente cambia el desplegable, nos envía el nombre de la isla
                    String requestedIsland = ctx.message();
                    activeSessions.put(ctx, requestedIsland);
                    System.out.println("📍 Cliente " + ctx.sessionId() + " mirando: " + requestedIsland);

                    // Le enviamos los datos de esa isla inmediatamente
                    ctx.send(gson.toJson(getRainFadeData(requestedIsland)));
                } catch (Exception e) {
                    System.err.println("❌ Error en onMessage: " + e.getMessage());
                }
            });

            ws.onClose(ctx -> {
                activeSessions.remove(ctx);
                System.out.println("❌ Cliente desconectado: " + ctx.sessionId());
            });

            ws.onError(ctx -> {
                System.err.println("⚠️ Error en WebSocket (" + ctx.sessionId() + "): " + ctx.error().getMessage());
            });
        });

        // Iniciamos el motor que "empuja" los datos a los clientes
        startWebSocketBroadcaster();
    }

    // Este hilo se ejecuta cada 2 segundos enviando el radar actualizado
    private void startWebSocketBroadcaster() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            for (Map.Entry<WsContext, String> entry : activeSessions.entrySet()) {
                WsContext ctx = entry.getKey();
                String island = entry.getValue();

                try {
                    // Si la conexión sigue abierta, le lanzamos el JSON
                    if (ctx.session.isOpen()) {
                        ctx.send(gson.toJson(getRainFadeData(island)));
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error en broadcaster para " + ctx.sessionId() + ": " + e.getMessage());
                }
            }
        }, 0, 2, TimeUnit.SECONDS); // 2 segundos de refresco ultrarrápido
    }

    // Método auxiliar para empaquetar los datos
    private RainFadeResponse getRainFadeData(String location) {
        List<RainFadeResponse.Prediction> predictions = service.getPredictionsForLocation(location);
        return new RainFadeResponse(location, Instant.now().toString(), predictions);
    }

    public void stop() {
        if (app != null) app.stop();
    }
}