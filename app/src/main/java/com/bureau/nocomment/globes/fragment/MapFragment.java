package com.bureau.nocomment.globes.fragment;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.model.ModelRepository;
import com.bureau.nocomment.globes.model.Project;
import com.bureau.nocomment.globes.model.Route;
import com.bureau.nocomment.globes.model.Table;
import com.cisco.cmx.model.CMXDimension;
import com.cisco.cmx.model.CMXFloor;
import com.cisco.cmx.model.CMXPath;
import com.cisco.cmx.model.CMXPoi;
import com.cisco.cmx.model.CMXPoint;
import com.cisco.cmx.ui.CMXFloorView;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class MapFragment extends BaseFragment implements CMXFloorView.SelectionHandler, CMXFloorView.ActiveSelectionHandler {

    private static final int kMAP_ID = R.drawable.plan_415;
    private static final int kTABLE_ID_OFFSET = 2000;

    @Bind(R.id.map)
    CMXFloorView mMapView;

    @Bind(R.id.quick_view)
    ViewGroup mQuickView;
    int mQuickViewHeight;

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
        mMapView.setSelectionHandler(this);
        mMapView.setActiveSelectionHandler(this);
        mMapView.setActivePoiMode(CMXFloorView.ActivePoiMode.CORONA);

        float bottomMargin = (float) getResources().getDimensionPixelSize(R.dimen.quickview_height);
        mMapView.setBottomMargin(bottomMargin);

        mMapView.setMarkerScalingTransform(new CMXFloorView.MarkerScalingFactorTransform() {
            @Override
            public float scalingFactorForScale(float scale) {
                // scale factor varies between 1.0 (full map) and ~21 (max zoom)
                // we want marker to be twice their intrinsic size at max zoom,
                // and half of intrinsic size at full scale
                float scalingFactor = (2.f - 0.5f) / (21.f - 1.f) * (scale - 1.f) + 0.5f;
                return scalingFactor;
            }
        });

        List<Project> projects = ModelRepository.getInstance().getItemLibrary().getProjects();
        for (Project project : projects) {
            addProjectMarker(project);
        }
        List<Table> tables = ModelRepository.getInstance().getItemLibrary().getTables();
        for (Table table : tables) {
            addTableMarker(table);
        }

        return rootView;
    }

    public void focusOnProject(Project project) {
        CMXPoi poi = makeCMXPoi(project.getId(), project.getX(), project.getY());
        mMapView.centerOnPoi(poi);
    }

    public void showRoute(Route route) {
        CMXPath path = makeCMXPath(route);
        mMapView.setPath(path);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Fragment childFragment = new MiniDetailsFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.quick_view, childFragment).commit();
    }

    @Override
    protected void onViewDrawn() {
        super.onViewDrawn();
        mQuickViewHeight = mQuickView.getHeight();
        hideMiniDetails(false);
    }

    private void addProjectMarker(Project project) {
        if (project.hasValidCoordinates()) {
            addMarker(project.getId(), project.getX(), project.getY(), R.drawable.ic_project_marker_transparent);
        }
    }

    private void addTableMarker(Table table) {
        int markerId = table.getId() + kTABLE_ID_OFFSET;
        addMarker(markerId, table.getX(), table.getY(), R.drawable.sticker_nfc);
    }

    private void addMarker(int id, float x, float y, int iconId) {
        Bitmap poiBitmap = BitmapFactory.decodeResource(getResources(), iconId);
        CMXPoi poi = makeCMXPoi(id, x, y);
        mMapView.showPoi(poi, poiBitmap);
    }

    @NonNull
    private CMXPoi makeCMXPoi(int id, float x, float y) {
        CMXPoint point = new CMXPoint(x, y);
        CMXPoi poi = new CMXPoi();
        poi.setPoints(Arrays.asList(point));
        poi.setId(Integer.toString(id));
        return poi;
    }

    private CMXPath makeCMXPath(Route route) {
        CMXPath path = new CMXPath();
        for(int projectId : route.getProjects()) {
            Project project = ModelRepository.getInstance().getItemLibrary().findProject(projectId);
            CMXPoint point = new CMXPoint(project.getX(), project.getY());
            path.add(point);
        }
        path.sortByY();
        return path;
    }

    @Override
    public void onPoiSelected(String poiIdentifier) {
        if (poiIdentifier == null) {
            return;
        }
        int id = Integer.parseInt(poiIdentifier);
        Project project = ModelRepository.getInstance().getItemLibrary().findProject(id);
        if (project != null) {
            CMXPoi activePoi = makeCMXPoi(project.getId(), project.getX(), project.getY());
            mMapView.setActivePoi(activePoi);

            showMiniDetails();
            playProject(id);
        }

        // TODO at some point :
        // if (id > 0) {
        // startActivity(DetailActivity.makeTestIntent(getContext(), id));
        // }
    }

    @Override
    public void onActivePoiSelected(String poiIdentifier) {
        if (poiIdentifier == null) {
            hideMiniDetails(true);
            return;
        }
    }

    private void hideMiniDetails(boolean animated) {
        if (animated) {
            mQuickView.animate().translationY(mQuickViewHeight).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {}

                @Override
                public void onAnimationEnd(Animator animator) {
                    mQuickView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {}

                @Override
                public void onAnimationRepeat(Animator animator) {}
            }).start();

        } else {
            mQuickView.setTranslationY(mQuickViewHeight);
            mQuickView.setVisibility(View.GONE);
        }
    }

    private void showMiniDetails() {
        mQuickView.setVisibility(View.VISIBLE);
        mQuickView.animate().translationY(0.f).setListener(null).start();
    }

    private void playProject(int projectID) {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.quick_view);
        MiniDetailsFragment miniDetails = (MiniDetailsFragment)fragment;
        miniDetails.playProject(projectID);
    }
}
