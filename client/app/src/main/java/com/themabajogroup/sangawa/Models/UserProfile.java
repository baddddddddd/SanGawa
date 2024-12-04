package com.themabajogroup.sangawa.Models;

import java.util.HashMap;
import java.util.Map;

public class UserProfile {
    private String userId;
    private String email;
    private String username;
    private Float fencingRadius;
    private Float scanRadius;
    private final float DEFAULT_FENCING_RADIUS = 1000;
    private final float DEFAULT_SCANNING_RADIUS = 3000;


    public UserProfile(String userId, String email, String username, float fencingRadius, float scanRadius) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.fencingRadius = fencingRadius;
        this.scanRadius = scanRadius;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public float getFencingRadius() {
        if (fencingRadius == null) {
            return DEFAULT_FENCING_RADIUS;
        }
        return fencingRadius;
    }

    public void setFencingRadius(float fencingRadius) {
        this.fencingRadius = fencingRadius;
    }

    public float getScanRadius() {
        if (scanRadius == null) {
            return DEFAULT_SCANNING_RADIUS;
        }
        return scanRadius;
    }

    public void setScanRadius(float scanRadius) {
        this.scanRadius = scanRadius;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();

        map.put("email", email);
        map.put("username", username);
        map.put("fencingRadius", fencingRadius.toString());
        map.put("scanRadius", scanRadius.toString());

        return map;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
