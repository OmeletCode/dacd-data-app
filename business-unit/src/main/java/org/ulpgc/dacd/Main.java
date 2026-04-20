package org.ulpgc.dacd;

import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Business Unit (Datamart & API) ---");

        // 1. Levantamos el servidor web en el puerto 8080
        Javalin app = Javalin.create().start(8080);

        // 2. Creamos la ruta principal (Home)
        app.get("/", ctx -> {
            ctx.result("🚀 ¡API del Monitor Predictivo de Rain Fade funcionando correctamente!");
        });

        // 3. Dejamos preparada la ruta que usaremos en el futuro para devolver los datos
        app.get("/api/alertas", ctx -> {
            ctx.result("Aquí devolveremos un JSON con los satélites que van a sufrir cortes por el clima...");
        });
    }
}