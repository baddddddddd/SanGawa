package com.themabajogroup.sangawa.Models;

import android.view.MenuItem;

public class TaskType {

    public static final TaskType ACTIVE = new TaskType("ACTIVE");
    public static final TaskType DUE = new TaskType("DUE");
    public static final TaskType COMPLETE = new TaskType("COMPLETE");
    public static final TaskType PENDING = new TaskType("PENDING");
    public static final TaskType JOINED = new TaskType("JOINED");
    public static final TaskType NEARBY = new TaskType("NEARBY");

    private String name;

    private TaskType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setVisibilityFor(MenuItem requestTaskMenuItem, MenuItem cancelTaskMenuItem, MenuItem doneTaskMenuItem, MenuItem editTaskMenuItem, MenuItem deleteTaskMenuItem) {
        switch (this.name) {
            case "ACTIVE":
                requestTaskMenuItem.setVisible(false);
                cancelTaskMenuItem.setVisible(false);
                doneTaskMenuItem.setVisible(true);
                editTaskMenuItem.setVisible(true);
                deleteTaskMenuItem.setVisible(true);
                break;

            case "DUE":
                requestTaskMenuItem.setVisible(false);
                cancelTaskMenuItem.setVisible(false);
                doneTaskMenuItem.setVisible(true);
                editTaskMenuItem.setVisible(true);
                deleteTaskMenuItem.setVisible(false);
                break;

            case "COMPLETE":
                requestTaskMenuItem.setVisible(false);
                cancelTaskMenuItem.setVisible(false);
                doneTaskMenuItem.setVisible(false);
                editTaskMenuItem.setVisible(false);
                deleteTaskMenuItem.setVisible(true);
                break;

            case "PENDING":
                requestTaskMenuItem.setVisible(false);
                cancelTaskMenuItem.setVisible(true);
                doneTaskMenuItem.setVisible(false);
                editTaskMenuItem.setVisible(false);
                deleteTaskMenuItem.setVisible(false);
                break;

            case "JOINED":
                requestTaskMenuItem.setVisible(false);
                cancelTaskMenuItem.setVisible(false);
                doneTaskMenuItem.setVisible(true);
                editTaskMenuItem.setVisible(false);
                deleteTaskMenuItem.setVisible(true);
                break;

            case "NEARBY":
                requestTaskMenuItem.setVisible(true);
                cancelTaskMenuItem.setVisible(false);
                doneTaskMenuItem.setVisible(false);
                editTaskMenuItem.setVisible(false);
                deleteTaskMenuItem.setVisible(false);
                break;
        }
    }
}
