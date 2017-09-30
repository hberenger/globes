package com.bureau.nocomment.globes.model;

import java.util.List;

public class Project {
    private int id;
    private String description;
    private String subtitle;
    private String audioFile;
    private List<String> images;
    private List<Float> coordinates;

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getAudioFile() {
        return audioFile;
    }

    // Y coordinate is expressed in percentage of the map main dimension.
    // X coordinate can between 0 and 100.0 * smallDimension / mainDimension
    public boolean hasValidCoordinates() {
        if (coordinates == null || coordinates.size() != 2) {
            return false;
        }
        float y = getY();
        if (y < 0.f || y > 100.f) {
            return false;
        }
        float x = getX();
        if (x < 0.f) {
            return false;
        }
        return true;
    }

    public float getX() {
        return (coordinates.size() == 2) ? coordinates.get(0) : 0.f;
    }

    public float getY() {
        return (coordinates.size() == 2) ? coordinates.get(1) : 0.f;
    }

    public List<String> getImages() {
        return images;
    }
}
