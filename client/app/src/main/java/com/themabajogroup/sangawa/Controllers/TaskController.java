package com.themabajogroup.sangawa.Controllers;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.themabajogroup.sangawa.Models.TaskDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import kotlin.NotImplementedError;

public class TaskController {
    private static TaskController instance;
    private final FirebaseFirestore db;

    private TaskController() {
        db = FirebaseFirestore.getInstance();
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
                            // TK: handle case when task doesn't exist
                        }
                    } else {
                        // TK: handle case when task failed
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
                       // TK: handle case when task failed
                   }
                });

        return result;
    }

    public List<TaskDetails> getNearbyTasks(double lat, double lon) {
        throw new NotImplementedError();
    }
}
