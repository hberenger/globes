package com.bureau.nocomment.globes.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.adapter.RoutesAdapter;
import com.bureau.nocomment.globes.model.ModelRepository;
import com.bureau.nocomment.globes.model.Project;
import com.bureau.nocomment.globes.model.Route;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RoutesFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    static final String LOGTAG = "RoutesFragment";

    private Boolean mInverseSort = false;
    private int     mLastSortField = 0;

    public interface ProjectSelectedObserver {
        void onProjectSelected(Project p);
    }

    @Bind(R.id.routes_list) ListView      mRouteList;
    private                 RoutesAdapter mRoutesAdapter;

    private ProjectSelectedObserver mProjectSelectedObserver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_routes, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.routes_list);

        View footer = LayoutInflater.from(getContext()).inflate(R.layout.view_list_go_top_footer, listView, false);
        listView.addFooterView(footer);

        mRoutesAdapter = new RoutesAdapter(getContext());
        listView.setAdapter(mRoutesAdapter);

        listView.setOnItemClickListener(this);

        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mProjectSelectedObserver = (ProjectSelectedObserver) context;
        } catch (ClassCastException e) {
            Log.d(LOGTAG, "Could not attach context as ProjectSelected observer");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        populate();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // Private

    private void populate() {
        List<Route> routes = ModelRepository.getInstance().getItemLibrary().getRoutes();

        mRoutesAdapter.setRoutes(routes);
    }

    @OnClick(R.id.gotop)
    void onGoTop() {
        goTop();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        List<Project> projects = ModelRepository.getInstance().getItemLibrary().getProjects();
        Project p = projects.get(i);
        if (mProjectSelectedObserver != null) {
            mProjectSelectedObserver.onProjectSelected(p);
        }
    }


    private void updateSortDirection(int tappedFieldId) {
        if (mLastSortField == tappedFieldId) {
            mInverseSort = !mInverseSort;
        } else {
            mInverseSort = false;
        }
        mLastSortField = tappedFieldId;
    }

    private void goTop() {
        mRouteList.smoothScrollToPositionFromTop(0, 0);
    }
}
