package org.ulpgc.starlink.spacex.control;

import org.ulpgc.starlink.spacex.model.SatelliteEvent;

import java.util.List;

public interface SpaceXSupplier {
    List<SatelliteEvent> getSatellites();
}