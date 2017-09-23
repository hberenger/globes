package com.bureau.nocomment.globes.model;

import java.util.List;

public class Project {
    private int id;
    private String description;
    private String audioFile;
    private List<String> images;

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public List<String> getImages() {
        return images;
    }
}
