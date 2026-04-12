package org.ulpgc.dacd;

public class Main {
    public static void main(String[] args) {
        SpaceXSupplier supplier = new RestSpaceXSupplier();

        SpaceXController controller = new SpaceXController(supplier);

        System.out.println("--- Iniciando el Extractor Automático de Starlink ---");
        controller.execute();
    }
}