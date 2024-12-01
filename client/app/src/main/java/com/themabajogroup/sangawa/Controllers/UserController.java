package com.themabajogroup.sangawa.Controllers;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.themabajogroup.sangawa.Models.CollabDetails;
import com.themabajogroup.sangawa.Models.TaskDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UserController {
    private static UserController instance;
    private final FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private final TaskController taskController;
    private LatLng currentLocation;
    private Map<String, Map<String, CollabDetails>> collabRequests;

    private UserController() {
        mAuth = FirebaseAuth.getInstance();
        taskController = TaskController.getInstance();

        collabRequests = new HashMap<>();
    }

    public static UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }

        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(FirebaseUser user) {
        this.currentUser = user;
    }

    public CompletableFuture<List<TaskDetails>> fetchUserTasks() {
        // TODO: Potentially bad coupling

        String userId = currentUser.getUid();
        CompletableFuture<List<TaskDetails>> result = new CompletableFuture<>();
        taskController.getAllUserTasks(userId)
                .thenAccept(result::complete);

        return result;
    }

    public CompletableFuture<List<TaskDetails>> fetchNearbyTasks() {
        // TODO: Potentially bad coupling again, but whatever

        String userId = currentUser.getUid();
//        LatLng currentLocation = getCurrentLocation();
        LatLng currentLocation = new LatLng(13.7839623, 121.0740536); // TODO: temp location

        // TODO: Radius should be adjustable by user
        double radius = 3000;

        CompletableFuture<List<TaskDetails>> result = new CompletableFuture<>();
        taskController.getNearbyTasks(userId, currentLocation.latitude, currentLocation.longitude, radius)
                .thenAccept(result::complete);

        return result;
    }

    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Map<String, Map<String, CollabDetails>> getCollabRequests() {
        return collabRequests;
    }
}
