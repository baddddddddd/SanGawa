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

    // NOTE: cancelTaskMenuItem.setVisible(false); setted false for all as it won't be needed
    public void setVisibilityFor(MenuItem requestTaskMenuItem, MenuItem cancelTaskMenuItem, MenuItem doneTaskMenuItem, MenuItem editTaskMenuItem, MenuItem deleteTaskMenuItem, MenuItem messageMenuItem) {
        switch (this.name) {
            case "ACTIVE":
                requestTaskMenuItem.setVisible(false);
                cancelTaskMenuItem.setVisible(false);
                doneTaskMenuItem.setVisible(true);
                editTaskMenuItem.setVisible(true);
                deleteTaskMenuItem.setVisible(true);
                messageMenuItem.setVisible(true);
                break;

            case "DUE":
                requestTaskMenuItem.setVisible(false);
                cancelTaskMenuItem.setVisible(false);
                doneTaskMenuItem.setVisible(true);
                editTaskMenuItem.setVisible(true);
                deleteTaskMenuItem.setVisible(true);
                messageMenuItem.setVisible(true);
                break;

            case "COMPLETE":
                requestTaskMenuItem.setVisible(false);
                cancelTaskMenuItem.setVisible(false);
                doneTaskMenuItem.setVisible(false);
                editTaskMenuItem.setVisible(false);
                deleteTaskMenuItem.setVisible(true);
                messageMenuItem.setVisible(false);
                break;

            case "PENDING":
                requestTaskMenuItem.setVisible(false);
                cancelTaskMenuItem.setVisible(false);
                doneTaskMenuItem.setVisible(false);
                editTaskMenuItem.setVisible(false);
                deleteTaskMenuItem.setVisible(false);
                messageMenuItem.setVisible(false);
                break;

            case "JOINED":
                requestTaskMenuItem.setVisible(false);
                cancelTaskMenuItem.setVisible(false);
                doneTaskMenuItem.setVisible(true);
                editTaskMenuItem.setVisible(false);
                deleteTaskMenuItem.setVisible(true);
                messageMenuItem.setVisible(true);
                break;

            case "NEARBY":
                requestTaskMenuItem.setVisible(true);
                cancelTaskMenuItem.setVisible(false);
                doneTaskMenuItem.setVisible(false);
                editTaskMenuItem.setVisible(false);
                deleteTaskMenuItem.setVisible(false);
                messageMenuItem.setVisible(false);
                break;
        }
    }
}
