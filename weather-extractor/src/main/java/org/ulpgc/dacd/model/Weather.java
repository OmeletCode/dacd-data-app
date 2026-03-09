package org.ulpgc.dacd.model;
public record Weather(long timestamp, double temp, int humidity, double windSpeed, String description, String location) {}