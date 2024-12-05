package com.themabajogroup.sangawa.Controllers;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.themabajogroup.sangawa.Models.CollabRequest;
import com.themabajogroup.sangawa.Models.RequestDetails;
import com.themabajogroup.sangawa.Models.RequestStatus;
import com.themabajogroup.sangawa.Models.TaskDetails;
import com.themabajogroup.sangawa.Models.TaskStatus;
import com.themabajogroup.sangawa.Utils.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public CompletableFuture<Boolean> editUserTaskStatus(String taskId, TaskStatus status) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", status.name());

        db.collection("tasks")
                .document(taskId)
                .update(updateData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        result.complete(true);
                    } else {
                        result.completeExceptionally(task.getException());
                    }
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

    public CompletableFuture<Boolean> createJoinRequest(String ownerId, String taskId, String requesterId, String requesterName) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        CollabRequest collabRequest = new CollabRequest(requesterName, RequestStatus.PENDING);

        realtimeDb.child("requests")
                .child(ownerId)
                .child(taskId)
                .child(requesterId)
                .setValue(collabRequest)
                .addOnCompleteListener(rtdbTask -> {
                    if (!rtdbTask.isSuccessful()) {
                        result.complete(false);
                        return;
                    }

                    Map<String, Object> requestDetails = new HashMap<>();
                    requestDetails.put("requesterId", requesterId);
                    requestDetails.put("ownerId", ownerId);
                    requestDetails.put("taskId", taskId);
                    requestDetails.put("status", RequestStatus.PENDING.name());

                    db.collection("requests")
                            .add(requestDetails)
                            .addOnCompleteListener(firestoreTask -> {
                                result.complete(firestoreTask.isSuccessful());
                            });
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
                .addOnCompleteListener(rtdbTask -> {
                    if (!rtdbTask.isSuccessful()) {
                        result.complete(false);
                        return;
                    }

                    Map<String, Object> requestDetails = new HashMap<>();
                    requestDetails.put("requesterId", requesterId);
                    requestDetails.put("ownerId", ownerId);
                    requestDetails.put("taskId", taskId);
                    requestDetails.put("status", status.name());

                    db.collection("requests")
                            .whereEqualTo("requesterId", requesterId)
                            .whereEqualTo("taskId", taskId)
                            .get()
                            .addOnCompleteListener(firestoreTask -> {
                                if (!firestoreTask.isSuccessful()) {
                                    result.complete(false);
                                    return;
                                }

                                WriteBatch batch = db.batch();
                                for (QueryDocumentSnapshot document : firestoreTask.getResult()) {
                                    batch.update(document.getReference(), requestDetails);
                                    break;
                                }

                                batch.commit().addOnCompleteListener(task -> {
                                    result.complete(task.isSuccessful());
                                });
                            });
                });

        return result;
    }

    public CompletableFuture<Boolean> removeJoinRequest(String ownerId, String taskid, String requesterId) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        String path = String.format("requests/%s/%s/%s", ownerId, taskid, requesterId);

        realtimeDb.child(path).setValue(null).addOnCompleteListener(task -> {
            result.complete(task.isSuccessful());
        });

        return result;
    }

    public CompletableFuture<List<RequestDetails>> getRequestHistory(String userId) {
        CompletableFuture<List<RequestDetails>> result = new CompletableFuture<>();
        db.collection("requests")
                .whereEqualTo("requesterId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }

                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    ArrayList<RequestDetails> requests = new ArrayList<>();

                    for (DocumentSnapshot document : documents) {
                        RequestDetails requestDetails = RequestDetails.fromDocumentSnapshot(document);
                        requests.add(requestDetails);
                    }

                    result.complete(requests);
                });

        return result;
    }

    public void attachJoinRequestListener(String userId, ValueEventListener listener) {
        realtimeDb.child("requests").child(userId)
                .getRef()
                .addValueEventListener(listener);
    }

    public void attachCollabReplyListener(String requesterId, TaskDetails taskDetails, ValueEventListener listener) {
        realtimeDb.child("requests")
                .child(taskDetails.getUserId())
                .child(taskDetails.getTaskId())
                .child(requesterId)
                .getRef()
                .addValueEventListener(listener);
    }
}
