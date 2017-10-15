package com.bureau.nocomment.globes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.adapter.ArchitectsAdapter;
import com.bureau.nocomment.globes.application.Globes;
import com.bureau.nocomment.globes.model.ModelRepository;
import com.bureau.nocomment.globes.model.Project;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ArchitectsFragment extends TabFragment {

    @Bind(R.id.architects_list) ListView          mArchitectsList;
    private                     ArchitectsAdapter mArchitectsAdapter;

    @Override
    public String getTabName() {
        return Globes.getAppContext().getResources().getString(R.string.tab_architects);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_architects, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.architects_list);

        mArchitectsAdapter = new ArchitectsAdapter(getContext());
        listView.setAdapter(mArchitectsAdapter);

        ButterKnife.bind(this, rootView);
        return rootView;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        populate();
    }

    private void populate() {
        List<Project> projects = ModelRepository.getInstance().getItemLibrary().getProjects();

        mArchitectsAdapter.setProjects(projects);
        mArchitectsAdapter.clear();
        mArchitectsAdapter.addAll(projects);
    }
}
