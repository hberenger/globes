package com.bureau.nocomment.globes.model;

import java.util.List;

public class Table {
    private int         id;
    private String      title;
    private String      subtitle;
    private String      audioFile;
    private List<Float> coordinates;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subtitle;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public float getX() {
        return (coordinates.size() == 2) ? coordinates.get(0) : 0.f;
    }

    public float getY() {
        return (coordinates.size() == 2) ? coordinates.get(1) : 0.f;
    }
}
