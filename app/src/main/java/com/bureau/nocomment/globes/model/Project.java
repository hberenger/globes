package com.bureau.nocomment.globes.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@JsonIgnoreProperties({"countryLabel"})
public class Project {
    private int          id;
    private int          index;
    private String       name;
    private String       author;
    private String       authorSortKey;
    private String       localization; // optional
    private String       countryCode;  // mandatory
    @JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy")
    public  Date         date;         // mandatory
    private String       dateDesc;     // optional
    private String       diamDesc;     // optional
    private Float        diameter;     // mandatory
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

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorSortKey() { return authorSortKey.isEmpty() ? author : authorSortKey; }

    public String getLocalizationDescription() {
        if (localization != null) {
            return localization;
        }
        return getCountryLabel();
    }

    private String getCountryLabel() {
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
        if(dateDesc != null && !dateDesc.isEmpty()) {
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

    public boolean matchesCompositePattern(String compositePattern) {
        String[] patterns = compositePattern.split("\\s+");
        for (String pattern : patterns) {
            if (!matchesPattern(pattern)) {
                return false;
            }
        }
        return true;
    }

    public boolean matchesPattern(String pattern) {
        if (pattern == null) {
            return true;
        }

        String s = normalize(pattern);

        if (contains(getName(), s)) {
            return true;
        }
        if (contains(getAuthor(), s)) {
            return true;
        }
        if (contains(getLocalizationDescription(), s)) {
            return true;
        }
        if (contains(getDateDescription(), s)) {
            return true;
        }
        if (contains(getDescription(), s)) {
            return true;
        }
        if (contains(getCountryLabel(), s)) {
            return true;
        }
        return false;
    }

    private static boolean contains(String searchString, String normalizedPattern) {
        if (searchString == null) {
            return false;
        }
        String normalizedSearchString = normalize(searchString);
        return normalizedSearchString.contains(normalizedPattern);
    }

    private static String normalize(String s) {
        return Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public String getDiameterDescription() {
        if (diamDesc != null && !diamDesc.isEmpty()) {
            return diamDesc;
        }
        String diamDesc = String.format(Locale.getDefault(), "%.0fm", getDiameter());
        return diamDesc;
    }
}
