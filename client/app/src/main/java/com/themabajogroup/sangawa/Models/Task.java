package com.themabajogroup.sangawa.Models;
// NOTE: this is temporary for ui testing and will later be change to actual TaskDescription class
public class Task {
    private final String title;
    private final String description;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}

