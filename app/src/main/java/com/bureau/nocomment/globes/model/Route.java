package com.bureau.nocomment.globes.model;

import java.util.List;

public class Route {
    private int           id;
    private String        title;
    private String        description;
    private List<Integer> projects;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Integer> getProjects() {
        return projects;
    }

    public int getLength() {
        return projects.size();
    }
}
