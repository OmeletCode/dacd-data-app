package org.ulpgc.space_extractor;

import org.ulpgc.space_extractor.model.SatelliteEvent;

import java.util.List;

public interface SpaceXSupplier {
    List<SatelliteEvent> getSatellites();
}