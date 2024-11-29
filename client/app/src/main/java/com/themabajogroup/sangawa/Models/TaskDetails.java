package com.themabajogroup.sangawa.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskDetails {
    private String taskId;
    private String userId;
    private String title;
    private String description;
    private Date deadline;
    private Date dateCreated;
    private double locationLat;
    private double locationLon;
    private TaskVisibility visibility;
    private TaskStatus status;

    public TaskDetails(String userId, String title, String description, double locationLat, double locationLon, TaskVisibility visibility, Date deadline) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
        this.visibility = visibility;
        this.status = TaskStatus.PENDING;
        this.deadline = deadline;
        this.dateCreated = new Date();
    }

    public static TaskDetails fromDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        Map<String, Object> data = documentSnapshot.getData();
        TaskDetails taskDetails = new TaskDetails(
                (String) data.get("userId"),
                (String) data.get("title"),
                (String) data.get("description"),
                (Double) data.get("locationLat"),
                (Double) data.get("locationLon"),
                TaskVisibility.valueOf((String) data.get("visibility")),
                ((Timestamp) data.get("deadline")).toDate()
        );

        taskDetails.setDateCreated(((Timestamp) data.get("dateCreated")).toDate());
        taskDetails.setStatus(TaskStatus.valueOf((String) data.get("status")));

        return taskDetails;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

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

    @Override
    public String toString() {
        return "TaskDetails{" +
                "userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", deadline=" + deadline +
                ", dateCreated=" + dateCreated +
                ", locationLat=" + locationLat +
                ", locationLon=" + locationLon +
                ", visibility=" + visibility +
                ", status=" + status +
                '}';
    }
}
