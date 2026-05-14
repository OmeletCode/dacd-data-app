package org.ulpgc.starlink.monitor.control;

import com.google.gson.JsonObject;
import org.ulpgc.starlink.monitor.model.SatelliteEvent;
import org.ulpgc.starlink.monitor.model.WeatherEvent;

import java.util.List;

public interface DataMart {
    void initTables();
    void save(JsonObject event);
    void updateServiceHealth(String serviceName, String timestamp);
    String getServiceLastSeen(String serviceName);
    WeatherEvent getLatestWeatherByTime(String location, String maxTimestamp);
    List<SatelliteEvent> getActiveSatellitesByTime(int limit, String maxTimestamp);
    List<Double> getTempHistory(String location);
}
