package com.bureau.nocomment.globes.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@JsonIgnoreProperties({"countryLabel"})
public class Project {
    private int          id;
    private String       name;
    private String       author;
    private String       localization; // optional
    private String       countryCode;  // mandatory
    @JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy")
    public  Date         date;         // mandatory
    private String       dateDesc;     // optional
    private Float        diameter;
    private String       description;
    private String       subtitle;
    private String       audioFile;
    private List<String> images;
    private List<Float>  coordinates;

    // computed properties
    private String       countryLabel;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getLocalizationDescription() {
        if (localization != null) {
            return localization;
        }
        if (countryLabel == null) {
            Locale loc = new Locale("", countryCode);
            countryLabel = loc.getDisplayCountry();
        }
        return countryLabel;
    }

    public Date getDate() {
        return date;
    }

    public String getDateDescription() {
        if(dateDesc != null) {
            return dateDesc;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        String year = formatter.format(date);
        return year;
    }

    public Float getDiameter() {
        return diameter;
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

    public void resetLocalizedInfo() {
        // reset locale-dependant computed properties
        countryLabel = null;
    }
}
