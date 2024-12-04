package com.themabajogroup.sangawa.Overlays;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.themabajogroup.sangawa.Models.MessageDetails;
import com.themabajogroup.sangawa.R;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<MessageDetails> messageDetailsList;
    private final String currentUserId;

    private static final int TYPE_CURRENT_USER = 1;
    private static final int TYPE_OTHER_USER = 2;

    public ChatListAdapter(List<MessageDetails> messageDetailsList, String currentUserId) {
        this.messageDetailsList = messageDetailsList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        MessageDetails message = messageDetailsList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return TYPE_CURRENT_USER;
        } else {
            return TYPE_OTHER_USER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_CURRENT_USER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.your_chat_item, parent, false);
            return new CurrentUserViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.other_user_chat_item, parent, false);
            return new OtherUserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageDetails messageDetails = messageDetailsList.get(position);
        if (holder.getItemViewType() == TYPE_CURRENT_USER) {
            ((CurrentUserViewHolder) holder).bind(messageDetails);
        } else {
            ((OtherUserViewHolder) holder).bind(messageDetails);
        }
    }

    @Override
    public int getItemCount() {
        return messageDetailsList.size();
    }

    public void addMessage(MessageDetails messageDetails) {
        messageDetailsList.add(messageDetails);
        notifyItemInserted(messageDetailsList.size() - 1);
    }

    public static class CurrentUserViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        public CurrentUserViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.your_message);
        }

        public void bind(MessageDetails messageDetails) {
            messageTextView.setText(messageDetails.getContent());
        }
    }

    public static class OtherUserViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;
        private TextView senderNameTextView;

        public OtherUserViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.others_message);
            senderNameTextView = itemView.findViewById(R.id.username);
        }

        public void bind(MessageDetails messageDetails) {
            messageTextView.setText(messageDetails.getContent());
            senderNameTextView.setText(messageDetails.getSenderName());
        }
    }
}
