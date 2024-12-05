package com.themabajogroup.sangawa.Overlays;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.themabajogroup.sangawa.Controllers.ChatController;
import com.themabajogroup.sangawa.Controllers.UserController;
import com.themabajogroup.sangawa.Models.MessageDetails;
import com.themabajogroup.sangawa.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatDialog extends Dialog {

    private final String currentUserId;
    private final String taskId;
    private final String title;
    private ChatController chatController;
    private RecyclerView recyclerViewMessages;
    private TextInputEditText inputMessage;

    public ChatDialog(Context context, String taskId, String currentUserId, String title) {
        super(context);
        this.taskId = taskId;
        this.currentUserId = currentUserId;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_chatroom);
        Objects.requireNonNull(getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chatController = ChatController.getInstance();

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        List<MessageDetails> messageDetailsList = new ArrayList<>();

        inputMessage = findViewById(R.id.message);

        ChatListAdapter adapter = new ChatListAdapter(messageDetailsList, currentUserId);
        recyclerViewMessages.setAdapter(adapter);

        getChatHistory(taskId, messageDetailsList, adapter);

        ImageButton closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dismiss());

        ImageButton sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(v -> {
            String messageContent = Objects.requireNonNull(inputMessage.getText()).toString().trim();
            if (!messageContent.isEmpty()) sendMessage(messageContent);
            else Toast.makeText(getContext(), "Message cannot be empty!", Toast.LENGTH_SHORT).show();
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void getChatHistory(String taskId, List<MessageDetails> messageDetailsList, ChatListAdapter adapter) {
        chatController.getChatroomHistory(taskId)
                .thenAccept(messages -> {
                    messageDetailsList.addAll(messages);
                    adapter.notifyDataSetChanged();
                    recyclerViewMessages.scrollToPosition(messageDetailsList.size() - 1);
                });

        chatController.onMessageReceived(taskId, message -> {
            messageDetailsList.add(message);
            adapter.notifyItemInserted(messageDetailsList.size() - 1);
            recyclerViewMessages.scrollToPosition(messageDetailsList.size() - 1);
        });
    }

    private void sendMessage(String messageContent) {
        UserController userController = UserController.getInstance();
        MessageDetails messageDetails = new MessageDetails(currentUserId, userController.getProfile().getUsername(), taskId, messageContent);
        chatController.sendMessage(messageDetails)
                .thenAccept(success -> {
                    if (success) {
                        Toast.makeText(getContext(), "Message sent", Toast.LENGTH_SHORT).show();
                        inputMessage.setText("");
                    } else {
                        Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
