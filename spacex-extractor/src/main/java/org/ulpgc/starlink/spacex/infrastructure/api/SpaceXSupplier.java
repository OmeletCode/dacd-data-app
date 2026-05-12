package org.ulpgc.starlink.spacex.infrastructure.api;

import org.ulpgc.starlink.spacex.model.SatelliteEvent;

import java.util.List;

public interface SpaceXSupplier {
    List<SatelliteEvent> getSatellites();
}