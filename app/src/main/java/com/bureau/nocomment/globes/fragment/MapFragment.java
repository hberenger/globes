package com.bureau.nocomment.globes.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.application.Globes;
import com.cisco.cmx.model.CMXDimension;
import com.cisco.cmx.model.CMXFloor;
import com.cisco.cmx.ui.CMXFloorView;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class MapFragment extends BaseFragment {

    private static final int kMAP_ID = R.drawable.plan2d;

    @Bind(R.id.map)
    CMXFloorView mMapView;

    @Override
    public String getTabName() {
        return Globes.getAppContext().getResources().getString(R.string.tab_map);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_map, container, false);
        final Bitmap mapImage = BitmapFactory.decodeResource(getResources(), kMAP_ID);

        ButterKnife.bind(this, rootView);

        float height = 100.0f; // see it at percent or meters
        float width = height * mapImage.getWidth() / mapImage.getHeight();
        // Coordinates will be O..width in X and 0..height in Y, with top-left origin, and Y axis pointing downwards
        CMXDimension dimensions = new CMXDimension(width, height, 0.0f, 0.f, 0.0f, CMXDimension.Unit.FEET);
        CMXFloor floor = new CMXFloor();
        floor.setDimension(dimensions);

        mMapView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        mMapView.setFloor(floor, mapImage);

        return rootView;
    }
}
