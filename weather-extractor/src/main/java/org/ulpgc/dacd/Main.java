package org.ulpgc.dacd;

import org.ulpgc.dacd.control.WeatherSupplier;
import org.ulpgc.dacd.model.Weather;

public class Main {
    public static void main(String[] args) {
        String key = "abe03c99d7dcf2d52fa36ac4b4622a66";
        WeatherSupplier supplier = new WeatherSupplier(key);

        try {
            // LPGC
            Weather data = supplier.get(28.12, -15.43);

            System.out.println("Resultados de la extraccion:");
            System.out.println(data.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}