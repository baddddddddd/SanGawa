package com.themabajogroup.sangawa.Controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AuthController {
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private static AuthController instance;


    private AuthController() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("username", username);

                        db.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(unused -> {
                                    result.complete(true);
                                });

                    } else {
                        result.complete(false);
                    }


                });

        return result;
    }
}
