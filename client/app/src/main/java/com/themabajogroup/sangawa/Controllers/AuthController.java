package com.themabajogroup.sangawa.Controllers;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.CompletableFuture;

public class AuthController {
    private FirebaseAuth mAuth;
    private static AuthController instance;

    private AuthController() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static AuthController getInstance() {
        if (instance == null) {
            instance = new AuthController();
        }

        return instance;
    }

    public CompletableFuture<Boolean> verifyCredentials(String email, String password) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task ->  {
                    result.complete(task.isSuccessful());
                });

        return result;
    }

    public CompletableFuture<Boolean> registerCredentials(String username, String email, String password) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task ->  {
                    result.complete(task.isSuccessful());
                });

        return result;
    }
}
