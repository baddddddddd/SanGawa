package com.themabajogroup.sangawa.Activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.themabajogroup.sangawa.Adapters.TaskAdapter;
import com.themabajogroup.sangawa.Models.Task;
import com.themabajogroup.sangawa.R;

import java.util.ArrayList;

public class TasksActivity extends AppCompatActivity {

        private ArrayList<Task> taskList;
        private TaskAdapter taskAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_tasks);

            RecyclerView recyclerView = findViewById(R.id.recyclerViewTasks);
            Button createTaskButton = findViewById(R.id.buttonCreateTask);

            taskList = new ArrayList<>();
            taskAdapter = new TaskAdapter(this, taskList);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(taskAdapter);

            createTaskButton.setOnClickListener(v -> {
                taskList.add(new Task("New Task", "Description of the task."));
                taskAdapter.notifyItemInserted(taskList.size() - 1);
            });
        }
    }
