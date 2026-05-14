package org.ulpgc.starlink.spacex;

import org.ulpgc.starlink.spacex.control.SpaceXController;
import org.ulpgc.starlink.spacex.control.RestSpaceXSupplier;
import org.ulpgc.starlink.spacex.control.SpaceXSupplier;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Starting Automatic SpaceX Extractor ---");

        SpaceXSupplier supplier = new RestSpaceXSupplier();
        SpaceXController controller = new SpaceXController(supplier);

        controller.execute();
    }
}
