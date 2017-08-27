package com.bureau.nocomment.globes.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bureau.nocomment.globes.R;

public class ArchitectsFragment extends BaseFragment {
    @Override
    public String getTabName() {
        return "Architectes";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_architects, container, false);
        return rootView;
    }
}
