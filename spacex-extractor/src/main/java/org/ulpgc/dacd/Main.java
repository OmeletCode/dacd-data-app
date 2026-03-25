package org.ulpgc.dacd;

public class Main {
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:spacex_data.db";

        SpaceXSupplier supplier = new RestSpaceXSupplier();
        SpaceXSerializer serializer = new SqliteSpaceXSerializer(dbUrl);

        SpaceXController controller = new SpaceXController(supplier, serializer);

        System.out.println("--- Iniciando el Extractor Automático de Starlink ---");
        controller.execute();
    }
}