package com.themabajogroup.sangawa.Models;

public class UserProfile {
    private String userId;
    private String email;
    private String username;


    public UserProfile(String userId, String email, String username) {
        this.userId = userId;
        this.email = email;
        this.username = username;
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
}
