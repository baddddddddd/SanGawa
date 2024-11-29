package com.themabajogroup.sangawa.Utils;

public class Converter {
    public static double metersToLatitude(double meters) {
        return meters / 111111.0;
    }

    public static double metersToLongitude(double meters, double latitude) {
        double metersPerDegreeLongitude = 111320 * Math.cos(Math.toRadians(latitude));
        return meters / metersPerDegreeLongitude;
    }
}
