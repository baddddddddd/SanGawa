package com.themabajogroup.sangawa.Overlays;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.themabajogroup.sangawa.Models.Task;
import com.themabajogroup.sangawa.R;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private final ArrayList<Task> taskList;

    public TaskAdapter(Context context, ArrayList<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tasks_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());

        holder.editButton.setOnClickListener(v -> {
            // TODO: Add edit logic, for example launch an activity to edit the task.
            Toast.makeText(context, "Edit task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        });

        // Delete task action
        holder.deleteButton.setOnClickListener(v -> {
            taskList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, taskList.size());
            Toast.makeText(context, "Task deleted: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        TextView taskDescription;
        ImageButton editButton;
        ImageButton deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.textViewTaskTitle);
            taskDescription = itemView.findViewById(R.id.textViewTaskDescription);
            editButton = itemView.findViewById(R.id.buttonEditTask);
            deleteButton = itemView.findViewById(R.id.buttonDeleteTask);
        }
    }
}
