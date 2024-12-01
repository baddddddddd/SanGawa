package com.themabajogroup.sangawa.Overlays;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.themabajogroup.sangawa.Models.TaskDetails;
import com.themabajogroup.sangawa.R;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {

    private final List<TaskDetails> tasks;
    private final TaskItemClickListener taskItemClickListener;
    private final Boolean isCurrentUserTask;

    public TaskListAdapter(List<TaskDetails> tasks, TaskItemClickListener listener, Boolean isCurrentUserTask) {
        this.tasks = tasks;
        this.taskItemClickListener = listener;
        this.isCurrentUserTask = isCurrentUserTask;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        TaskDetails task = tasks.get(position);
        holder.textViewTaskTitle.setText(task.getTitle());
        holder.textViewTaskDescription.setText(task.getDescription());
        holder.buttonMoreOptions.setOnClickListener(v -> taskItemClickListener.onMoreOptionClick(v, task, isCurrentUserTask));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewTaskTitle, textViewTaskDescription;
        public TextView optionAcceptTask;
        public ImageButton buttonMoreOptions;

        public TaskViewHolder(View view) {
            super(view);
            textViewTaskTitle = view.findViewById(R.id.textViewTaskTitle);
            textViewTaskDescription = view.findViewById(R.id.textViewTaskDescription);
            buttonMoreOptions = view.findViewById(R.id.buttonMoreOptions);
            optionAcceptTask = view.findViewById(R.id.menu_accept_task);
        }
    }

    public interface TaskItemClickListener {
        void onMoreOptionClick(View view, TaskDetails task, Boolean isCurrentUserTask);
    }
}
