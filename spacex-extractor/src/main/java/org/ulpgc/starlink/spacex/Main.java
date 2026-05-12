package org.ulpgc.starlink.spacex;

import org.ulpgc.starlink.spacex.control.SpaceXController;
import org.ulpgc.starlink.spacex.infrastructure.api.RestSpaceXSupplier;
import org.ulpgc.starlink.spacex.infrastructure.api.SpaceXSupplier;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando el Extractor Automático de Starlink ---");

        SpaceXSupplier supplier = new RestSpaceXSupplier();
        SpaceXController controller = new SpaceXController(supplier);

        controller.execute();
    }
}