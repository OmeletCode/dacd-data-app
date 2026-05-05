package org.ulpgc.dacd;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando el Extractor Automático de Starlink ---");

        SpaceXSupplier supplier = new RestSpaceXSupplier();
        SpaceXController controller = new SpaceXController(supplier);

        controller.execute();
    }
}