package org.ulpgc.dacd;

public record Satellite(String name, double latitude, double longitude, double velocity, String rawJson) {
}