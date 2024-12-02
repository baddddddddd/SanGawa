package com.themabajogroup.sangawa.Utils;

import com.google.android.gms.maps.model.LatLng;

public class Converter {
    public static double metersToLatitude(double meters) {
        return meters / 111111.0;
    }

    public static double metersToLongitude(double meters, double latitude) {
        double metersPerDegreeLongitude = 111320 * Math.cos(Math.toRadians(latitude));
        return meters / metersPerDegreeLongitude;
    }

    public static double getDistance(LatLng a, LatLng b) {
        // Haversine formula

        final double EARTH_RADIUS = 6371000;

        double lat1 = Math.toRadians(a.latitude);
        double lon1 = Math.toRadians(a.longitude);
        double lat2 = Math.toRadians(b.latitude);
        double lon2 = Math.toRadians(b.longitude);

        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;
        double haversine = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));

        return EARTH_RADIUS * c;
    }
}
