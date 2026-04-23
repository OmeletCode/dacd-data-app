package org.ulpgc.dacd;

import org.ulpgc.dacd.model.SatelliteEvent;

import java.util.List;

public interface SpaceXSupplier {
    List<SatelliteEvent> getSatellites();
}