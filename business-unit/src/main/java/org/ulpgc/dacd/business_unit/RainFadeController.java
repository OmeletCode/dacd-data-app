package org.ulpgc.dacd.business_unit;

import io.javalin.Javalin;
import org.ulpgc.dacd.business_unit.model.RainFadeResponse;

import java.time.Instant;
import java.util.List;

public class RainFadeController {
    private final RainFadeService service;
    private Javalin app;

    public RainFadeController(RainFadeService service) {
        this.service = service;
    }

    public void start(int port) {
        app = Javalin.create(config -> config.staticFiles.add("/public")).start(port);

        app.get("/api/rainfade/{isla}", ctx -> {
            String location = ctx.pathParam("isla").replace("-", " ");

            List<RainFadeResponse.Prediction> predictions = service.getPredictionsForLocation(location);

            if (predictions.isEmpty()) {
                ctx.status(404).result("No hay datos climáticos registrados para: " + location);
                return;
            }

            RainFadeResponse response = new RainFadeResponse(location, Instant.now().toString(), predictions);
            ctx.json(response);
        });
    }

    public void stop() {
        if (app != null) app.stop();
    }
}