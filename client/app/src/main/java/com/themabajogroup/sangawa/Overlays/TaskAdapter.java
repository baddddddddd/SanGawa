package com.themabajogroup.sangawa.Overlays;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.themabajogroup.sangawa.Models.TaskDetails;
import com.themabajogroup.sangawa.R;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<TaskDetails> tasks;
    private final TaskItemClickListener taskItemClickListener;

    public TaskAdapter(List<TaskDetails> tasks, TaskItemClickListener listener) {
        this.tasks = tasks;
        this.taskItemClickListener = listener;
    }

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

        holder.buttonEditTask.setOnClickListener(v -> taskItemClickListener.onEditTaskClick(task));
        holder.buttonDeleteTask.setOnClickListener(v -> taskItemClickListener.onDeleteTaskClick(task));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewTaskTitle;
        public TextView textViewTaskDescription;
        public ImageButton buttonEditTask;
        public ImageButton buttonDeleteTask;

        public TaskViewHolder(View view) {
            super(view);
            textViewTaskTitle = view.findViewById(R.id.textViewTaskTitle);
            textViewTaskDescription = view.findViewById(R.id.textViewTaskDescription);
            buttonEditTask = view.findViewById(R.id.buttonEditTask);
            buttonDeleteTask = view.findViewById(R.id.buttonDeleteTask);
        }
    }

    public interface TaskItemClickListener {
        void onEditTaskClick(TaskDetails task);
        void onDeleteTaskClick(TaskDetails task);
    }
}
