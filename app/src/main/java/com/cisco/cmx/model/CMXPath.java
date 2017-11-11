package com.cisco.cmx.model;

import android.graphics.RectF;

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

    public RectF getBounds() {
        if (mPoints.size() == 0) {
            return new RectF(0.f, 0.f, 0.f, 0.f);
        }
        CMXPoint first = mPoints.get(0);
        RectF bounds = new RectF(first.getX(), first.getY(), first.getX(), first.getY());
        for (int i = 2; i < mPoints.size(); ++i) {
            CMXPoint pt = mPoints.get(i);
            bounds.union(pt.getX(), pt.getY());
        }
        return bounds;
    }
}
