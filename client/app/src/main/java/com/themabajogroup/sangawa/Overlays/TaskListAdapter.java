package com.themabajogroup.sangawa.Overlays;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.themabajogroup.sangawa.Controllers.TaskController;
import com.themabajogroup.sangawa.Models.RequestDetails;
import com.themabajogroup.sangawa.Models.TaskDetails;
import com.themabajogroup.sangawa.R;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {

    private final List<Object> items;
    private final TaskItemClickListener taskItemClickListener;
    private final Boolean isCurrentUserTask;
    private final TaskController taskController;

    public TaskListAdapter(List<?> items, TaskItemClickListener listener, Boolean isCurrentUserTask) {
        this.items = (List<Object>) items;
        this.taskItemClickListener = listener;
        this.isCurrentUserTask = isCurrentUserTask;
        this.taskController = TaskController.getInstance();
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
        Object item = items.get(position);

        if (item instanceof TaskDetails) {
            TaskDetails task = (TaskDetails) item;
            holder.textViewTaskTitle.setText(task.getTitle());
            holder.textViewTaskDescription.setText(task.getDescription());
            holder.buttonMoreOptions.setOnClickListener(v ->
                    taskItemClickListener.onMoreOptionClick(v, task, isCurrentUserTask)
            );
        } else if (item instanceof RequestDetails) {
            RequestDetails request = (RequestDetails) item;
            taskController.getUserTask(request.getTaskId())
                    .thenAccept(task -> {
                        holder.textViewTaskTitle.setText(task.getTitle());
                        holder.textViewTaskDescription.setText(task.getDescription());
                        holder.buttonMoreOptions.setOnClickListener(v ->
                                taskItemClickListener.onMoreOptionClick(v, task, isCurrentUserTask));
                    });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
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
            optionAcceptTask = view.findViewById(R.id.menu_request_task);
        }
    }

    public interface TaskItemClickListener {
        void onMoreOptionClick(View view, TaskDetails task, Boolean isCurrentUserTask);
    }
}
