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
import com.bureau.nocomment.globes.adapter.ArchitectsAdapter;
import com.bureau.nocomment.globes.model.ModelRepository;
import com.bureau.nocomment.globes.model.Project;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ArchitectsFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    static final String LOGTAG = "ArchitectsFragment";

    public interface ProjectSelectedObserver {
        void onProjectSelected(Project p);
    }

    @Bind(R.id.architects_list) ListView          mArchitectsList;
    private                     ArchitectsAdapter mArchitectsAdapter;

    private ProjectSelectedObserver mProjectSelectedObserver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_architects, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.architects_list);

        View footer = LayoutInflater.from(getContext()).inflate(R.layout.view_architects_footer, listView, false);
        listView.addFooterView(footer);

        mArchitectsAdapter = new ArchitectsAdapter(getContext());
        listView.setAdapter(mArchitectsAdapter);

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

    // Private

    private void populate() {
        List<Project> projects = ModelRepository.getInstance().getItemLibrary().getProjects();

        mArchitectsAdapter.setProjects(projects);
        mArchitectsAdapter.clear();
        mArchitectsAdapter.addAll(projects);
    }

    @OnClick(R.id.gotop)
    void onGoTop() {
        // TODO : go top
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        List<Project> projects = ModelRepository.getInstance().getItemLibrary().getProjects();
        Project p = projects.get(i - 1); // minus one because of header
        if (mProjectSelectedObserver != null) {
            mProjectSelectedObserver.onProjectSelected(p);
        }
    }
}
