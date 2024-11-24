package com.themabajogroup.sangawa.Models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskDetails {
    private String userId;
    private String title;
    private String description;
    private Date deadline;
    private Date dateCreated;
    private double locationLat;
    private double locationLon;
    private TaskVisibility visibility;
    private TaskStatus status;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLon() {
        return locationLon;
    }

    public void setLocationLon(double locationLon) {
        this.locationLon = locationLon;
    }

    public TaskVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(TaskVisibility visibility) {
        this.visibility = visibility;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", getUserId());
        map.put("title", getTitle());
        map.put("description", getDescription());
        map.put("deadline", getDeadline());
        map.put("dateCreated", getDateCreated());
        map.put("locationLat", getLocationLat());
        map.put("locationLon", getLocationLon());
        map.put("visibility", getVisibility().name());
        map.put("status", getStatus().name());
        return map;
    }
}
