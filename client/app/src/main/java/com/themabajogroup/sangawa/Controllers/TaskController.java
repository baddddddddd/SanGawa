package com.themabajogroup.sangawa.Controllers;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.themabajogroup.sangawa.Models.CollabRequest;
import com.themabajogroup.sangawa.Models.RequestStatus;
import com.themabajogroup.sangawa.Models.TaskDetails;
import com.themabajogroup.sangawa.Utils.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TaskController {
    private static TaskController instance;
    private final FirebaseFirestore db;
    private final DatabaseReference realtimeDb;

    private TaskController() {
        db = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance("https://sangawa-db-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    public static TaskController getInstance() {
        if (instance == null) {
            instance = new TaskController();
        }

        return instance;
    }

    public CompletableFuture<Boolean> createUserTask(TaskDetails taskDetails) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        db.collection("tasks")
                .add(taskDetails.toMap())
                .addOnCompleteListener(task -> {
                    result.complete(task.isSuccessful());
                });

        return result;
    }

    public CompletableFuture<TaskDetails> getUserTask(String taskId) {
        CompletableFuture<TaskDetails> result = new CompletableFuture<>();

        db.collection("tasks")
                .document(taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {
                            TaskDetails taskDetails = TaskDetails.fromDocumentSnapshot(document);
                            taskDetails.setTaskId(taskId);
                            result.complete(taskDetails);
                        } else {
                            // TODO: handle case when task doesn't exist
                        }
                    } else {
                        // TODO: handle case when task failed
                    }
                });

        return result;
    }

    public CompletableFuture<Boolean> editUserTask(String taskId, TaskDetails updatedTask) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        db.collection("tasks")
                .document(taskId)
                .update(updatedTask.toMap())
                .addOnCompleteListener(task -> {
                   result.complete(task.isSuccessful());
                });

        return result;
    }

    public CompletableFuture<Boolean> deleteUserTask(String taskId) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        db.collection("tasks")
                .document(taskId)
                .delete()
                .addOnCompleteListener(task -> {
                    result.complete(task.isSuccessful());
                });

        return result;
    }

    public CompletableFuture<List<TaskDetails>> getAllUserTasks(String userId) {
        CompletableFuture<List<TaskDetails>> result = new CompletableFuture<>();

        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       List<DocumentSnapshot> documents = task.getResult().getDocuments();
                       ArrayList<TaskDetails> tasks = new ArrayList<>();

                       for (DocumentSnapshot document : documents) {
                           TaskDetails taskDetails = TaskDetails.fromDocumentSnapshot(document);
                           String taskId = document.getId();
                           taskDetails.setTaskId(taskId);
                           tasks.add(taskDetails);
                       }

                       result.complete(tasks);
                   } else {
                       // TODO: handle case when task failed
                   }
                });

        return result;
    }

    public CompletableFuture<List<TaskDetails>> getNearbyTasks(String userId, double centerLat, double centerLon, double radius) {
        CompletableFuture<List<TaskDetails>> result = new CompletableFuture<>();

        double latitudeDistance = Converter.metersToLatitude(radius);
        double longitudeDistance = Converter.metersToLongitude(radius, centerLat);

        LatLng upperLeftBound = new LatLng(
                centerLat - latitudeDistance,
                centerLon - longitudeDistance
        );

        LatLng lowerRightBound = new LatLng(
                centerLat + latitudeDistance,
                centerLon + longitudeDistance
        );

        db.collection("tasks")
                .whereGreaterThanOrEqualTo("locationLat", upperLeftBound.latitude)
                .whereLessThanOrEqualTo("locationLat", lowerRightBound.latitude)
                .whereGreaterThanOrEqualTo("locationLon", upperLeftBound.longitude)
                .whereLessThanOrEqualTo("locationLon", lowerRightBound.longitude)
                .whereIn("visibility", Arrays.asList("OPEN_TO_ALL", "REQUEST_TO_JOIN"))
                .whereNotEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        ArrayList<TaskDetails> tasks = new ArrayList<>();

                        for (DocumentSnapshot document : documents) {
                            TaskDetails taskDetails = TaskDetails.fromDocumentSnapshot(document);
                            String taskId = document.getId();
                            taskDetails.setTaskId(taskId);
                            tasks.add(taskDetails);
                        }

                        result.complete(tasks);
                    } else {
                        // TODO: Handle when fetching nearby collaborative tasks failed
                    }
                });

        return result;
    }

    public CompletableFuture<Boolean> createJoinRequest(String requesterName, String requesterId, String ownerId, String taskId) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        CollabRequest requestDetails = new CollabRequest(requesterName, RequestStatus.PENDING);

        realtimeDb.child("requests")
                .child(ownerId)
                .child(taskId)
                .child(requesterId)
                .setValue(requestDetails)
                .addOnCompleteListener(task -> {
                    result.complete(task.isSuccessful());
                });

        return result;
    }

    public CompletableFuture<Boolean> updateJoinRequest(String ownerId, String taskId, String requesterId, RequestStatus status) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        realtimeDb.child("requests")
                .child(ownerId)
                .child(taskId)
                .child(requesterId)
                .child("status")
                .setValue(status)
                .addOnCompleteListener(task -> {
                    result.complete(task.isSuccessful());
                });

        return result;
    }

    public void attachJoinRequestListener(String userId, ValueEventListener listener) {
        realtimeDb.child("requests").child(userId)
                .getRef()
                .addValueEventListener(listener);
    }
}
