package com.themabajogroup.sangawa.Controllers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.themabajogroup.sangawa.Models.MessageDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import kotlin.NotImplementedError;

public class ChatController {
    private static ChatController instance;
    private DatabaseReference db;

    private ChatController() {
        db = FirebaseDatabase.getInstance("https://sangawa-db-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    public static ChatController getInstance() {
        if (instance == null) {
            instance = new ChatController();
        }

        return instance;
    }

    public CompletableFuture<List<MessageDetails>> getChatroomHistory(String taskId) {
        CompletableFuture<List<MessageDetails>> result = new CompletableFuture<>();
        List<MessageDetails> messages = new ArrayList<>();

        db.child("chatrooms").child(taskId)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    Map<String, Object> values = (Map<String, Object>) dataSnapshot.getValue();

                    if (values != null) {
                        for (Object message : values.values()) {
                            Map<String, String> map = (Map<String, String>) message;

                            MessageDetails messageDetails = MessageDetails.fromMap(map);
                            messages.add(messageDetails);
                        }
                    }

                    result.complete(messages);
                })
                .addOnFailureListener(runnable -> {
                    result.complete(messages);
                });

        return result;
    }

    public void onMessageReceived(String taskId, Consumer<MessageDetails> handler) {
        Date currentDate = new Date();

        db.child("chatrooms").child(taskId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Map<String, String> map = (Map<String, String>) snapshot.getValue();

                        if (map == null) {
                            return;
                        }

                        MessageDetails message = MessageDetails.fromMap(map);

                        if (message.getDateSent().after(currentDate)) {
                            handler.accept(message);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public CompletableFuture<Boolean> sendMessage(MessageDetails messageDetails) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        db.child("chatrooms").child(messageDetails.getTaskId())
                .push()
                .setValue(messageDetails.toMap())
                .addOnSuccessListener(runnable -> {
                    result.complete(true);
                })
                .addOnFailureListener(runnable -> {
                    result.complete(false);
                });

        return result;
    }
}
