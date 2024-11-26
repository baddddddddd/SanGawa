package com.themabajogroup.sangawa.Controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.themabajogroup.sangawa.Models.TaskDetails;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserController {
    private static UserController instance;
    private final FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private TaskController taskController;

    private UserController() {
        mAuth = FirebaseAuth.getInstance();
        taskController = TaskController.getInstance();
    }

    public static UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }

        return instance;
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
}
