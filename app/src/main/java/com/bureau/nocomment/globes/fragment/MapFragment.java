package com.bureau.nocomment.globes.fragment;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.common.Tagger;
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
import butterknife.OnClick;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class MapFragment extends BaseFragment implements CMXFloorView.SelectionHandler, CMXFloorView.ActiveSelectionHandler, MiniDetailsFragment.PlayerListener {

    private static final int kMAP_ID = R.drawable.plan_415;
    private static final int kTABLE_ID_OFFSET = 2000;
    private static final float kMAP_HEIGHT = 100.f;      // see it at percent or meters

    private static final String kMINI_DETAILS_TAG = "minidetails";

    private static final String TAG_CTX = "Map";

    @Bind(R.id.map)
    CMXFloorView mMapView;

    @Bind(R.id.quick_view)
    ViewGroup mQuickView;
    int mQuickViewHeight;

    @Bind(R.id.route_info)
    ViewGroup mRouteInfo;
    @Bind(R.id.route_name)
    TextView  mRouteName;

    @Bind(R.id.now_playing_info)
    ViewGroup mNowPlayingInfo;
    @Bind(R.id.current_track_name)
    TextView mCurrentTrackName;

    private Table tableToPlayOnResume;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_map, container, false);
        final Bitmap mapImage = BitmapFactory.decodeResource(getResources(), kMAP_ID);

        ButterKnife.bind(this, rootView);

        Drawable d = mNowPlayingInfo.getBackground();
        if (d instanceof GradientDrawable) {
            d.mutate();
            int primary = getResources().getColor(R.color.colorPrimary);
            int transparent = Color.argb(204, Color.red(primary), Color.green(primary), Color.blue(primary));
            ((GradientDrawable)d).setColor(transparent);
        }
        mNowPlayingInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNowPlayingTap();
            }
        });

        mRouteInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCurrentRouteTap();
            }
        });

        float height = kMAP_HEIGHT;
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
        mMapView.setCoronaColor(getResources().getColor(R.color.colorPrimary));
        int pathColor = getResources().getColor(R.color.pathColor);
        int pathPointColor = getResources().getColor(R.color.colorPrimary);
        mMapView.setPathColor(pathColor, 192, pathPointColor, 216);

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

        mMapView.setPathPointScalingTransform(new CMXFloorView.MarkerScalingFactorTransform() {
            @Override
            public float scalingFactorForScale(float scale) {
                float scalingFactor = (4.5f - 0.5f) / (21.f - 1.f) * (scale - 1.f) + 0.5f;
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

    // Public Fragment API

    public void focusOnProject(Project project) {
        setActiveProject(project, true);
    }

    public void showRoute(Route route) {
        CMXPath path = makeCMXPath(route);
        mMapView.setPath(path);
        centerOnPath(path);
        mRouteInfo.setVisibility(View.VISIBLE);
        mRouteName.setText(String.format(getString(R.string.route_info), route.getTitle()));
    }

    private void centerOnPath(CMXPath path) {
        RectF bounds = path.getBounds();
        float height = (bounds.height() > 0.f) ? bounds.height() : kMAP_HEIGHT;
        bounds.top =  bounds.top - 0.13f * height;           // give some...
        bounds.bottom = bounds.bottom + 0.05f * height;     // ... margin
        float zoomLevel = kMAP_HEIGHT / (height * 1.18f);   // 1.18 = 1 + 0.13 + 0.05
        if (zoomLevel > mMapView.getMaxScale()) {
            zoomLevel = mMapView.getMaxScale();
        }
        mMapView.centerOnPoint(bounds.centerX(), bounds.centerY(), zoomLevel);
    }

    private void hideRoute() {
        mMapView.setPath(null);
        mRouteInfo.setVisibility(View.GONE);
    }

    public void focusAndPlayTable(Table table) {
        // TODO : improve this test
        if (mMapView != null && getChildFragmentManager().findFragmentById(R.id.quick_view) != null) {
            setActiveTable(table, true, true);
        } else {
            tableToPlayOnResume = table;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MiniDetailsFragment childFragment = (MiniDetailsFragment) getChildFragmentManager().findFragmentByTag(kMINI_DETAILS_TAG);
        if (childFragment == null) {
            childFragment = new MiniDetailsFragment();
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.quick_view, childFragment, kMINI_DETAILS_TAG);
            transaction.commit();
        }
        childFragment.setPlayerListener(this);
    }

    @Override
    protected void onViewDrawn() {
        super.onViewDrawn();
        mQuickViewHeight = mQuickView.getHeight();
        hideMiniDetails(false);
        mMapView.focusOnTop(4.0f);
    }

    @Override
    public void reset() {
        hideMiniDetails(false);
        mMapView.setActivePoi(null);
        mMapView.focusOnTop(4.0f);
        hideRoute();
        hideNowPlayingInfo();
        // TODO : reset mini details
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
        if (id > kTABLE_ID_OFFSET) {
            Table table = ModelRepository.getInstance().getItemLibrary().findTable(id - kTABLE_ID_OFFSET);
            if (table != null) {
                Tagger.getInstance().tag(TAG_CTX, "poi_tap t " + table.getId());
                setActiveTable(table, false, false);
            }

        } else {
            Project project = ModelRepository.getInstance().getItemLibrary().findProject(id);
            if (project != null) {
                Tagger.getInstance().tag(TAG_CTX, "poi_tap p " + project.getId());
                setActiveProject(project, false);
            }
        }
    }

    @Override
    public void onActivePoiSelected(String poiIdentifier) {
        if (poiIdentifier == null) {
            Tagger.getInstance().tag(TAG_CTX, "empty_tap");
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

    private void setActiveProject(Project project, boolean centerProjectInView) {
        CMXPoi activePoi = makeCMXPoi(project.getId(), project.getX(), project.getY());
        mMapView.setActivePoi(activePoi);
        if (centerProjectInView) {
            mMapView.centerOnPoi(activePoi);
        }
        showMiniDetails();
        showProject(project);
    }

    private void showProject(Project project) {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.quick_view);
        MiniDetailsFragment miniDetails = (MiniDetailsFragment)fragment;
        miniDetails.showProject(project.getId());
    }

    private void setActiveTable(Table table, boolean focusOnTable, boolean play) {
        // TODO (not so important) factorize with project
        CMXPoi activePoi = makeCMXPoi(table.getId() + kTABLE_ID_OFFSET, table.getX(), table.getY());
        mMapView.setActivePoi(activePoi);
        if (focusOnTable) {
            mMapView.centerOnPoi(activePoi);
        }

        showMiniDetails();
        showTable(table, play);
    }

    private void showTable(Table table, boolean play) {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.quick_view);
        MiniDetailsFragment miniDetails = (MiniDetailsFragment)fragment;
        miniDetails.showTable(table.getId(), play);
    }

    private void hideNowPlayingInfo() {
        mNowPlayingInfo.setVisibility(View.GONE);
    }

    @Override
    public void playerDidStartToPlay(int trackId) {
        Bitmap poiBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.table_playing);
        String poiId = Integer.toString(trackId + kTABLE_ID_OFFSET);
        mMapView.updatePoiBitmap(poiId, poiBitmap);
        mNowPlayingInfo.setVisibility(View.VISIBLE);
        Table table = ModelRepository.getInstance().getItemLibrary().findTable(trackId);
        if (table != null) {
            mCurrentTrackName.setText(String.format(getString(R.string.now_playing_info), table.getTitle()));
        }
    }

    @Override
    public void playerDidPause(int trackId) {
        hideNowPlayingInfo();
    }

    @Override
    public void playerDidEndToPlay(int trackId) {
        Bitmap poiBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_nfc);
        String poiId = Integer.toString(trackId + kTABLE_ID_OFFSET);
        mMapView.updatePoiBitmap(poiId, poiBitmap);
        hideNowPlayingInfo();
    }

    @Override
    public void onReadyToPlay() {
        if (tableToPlayOnResume != null) {
            final Table tableToPlay = tableToPlayOnResume;
            tableToPlayOnResume = null;
            final Handler handler = new Handler();
            // TODO : Hacky delay
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setActiveTable(tableToPlay, true, true);
                }
            }, 1000);
        }
    }

    @OnClick(R.id.route_close)
    void onRouteClose(ImageButton button) {
        Tagger.getInstance().tag(TAG_CTX, "close_current_route");
        hideRoute();
    }

    private void onNowPlayingTap() {
        Tagger.getInstance().tag(TAG_CTX, "now_playing_tap");
        showMiniDetails();
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.quick_view);
        MiniDetailsFragment miniDetails = (MiniDetailsFragment)fragment;
        miniDetails.showCurrentTable();
    }

    private void onCurrentRouteTap() {
        Tagger.getInstance().tag(TAG_CTX, "current_route_tap");
        CMXPath path = mMapView.getCurrentPath();
        if (path != null) {
            centerOnPath(path);
        }
    }
}
