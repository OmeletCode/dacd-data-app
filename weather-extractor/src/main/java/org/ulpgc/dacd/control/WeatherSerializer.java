package org.ulpgc.dacd.control; // O el paquete donde la pongas

import org.ulpgc.dacd.model.Weather; // Asegúrate de importar tu clase Weather

public interface WeatherSerializer {
    void saveWeather(Weather weather);
}