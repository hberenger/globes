package com.cisco.cmx.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CMXPath {

    private List<CMXPoint> mPoints = new ArrayList<CMXPoint>();

    public void add(CMXPoint point) {
        mPoints.add(point);
    }

    public List<CMXPoint> getPoints() {
        return mPoints;
    }

    public void sortByY() {
        Collections.sort(mPoints, new Comparator<CMXPoint>() {
            @Override
            public int compare(CMXPoint lhs, CMXPoint rhs) {
                Float lhsY = lhs.getY();
                Float rhsY = rhs.getY();

                return lhsY.compareTo(rhsY);
            }
        });
    }
}
