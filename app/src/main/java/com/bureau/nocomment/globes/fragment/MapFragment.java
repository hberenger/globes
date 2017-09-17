package com.bureau.nocomment.globes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.application.Globes;

public class MapFragment extends BaseFragment {
    @Override
    public String getTabName() {
        return Globes.getAppContext().getResources().getString(R.string.tab_map);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_map, container, false);
        return rootView;
    }
}
