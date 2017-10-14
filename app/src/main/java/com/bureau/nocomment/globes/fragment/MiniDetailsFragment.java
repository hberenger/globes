package com.bureau.nocomment.globes.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bureau.nocomment.globes.R;

public class MiniDetailsFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_minidetails, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // maybe check whether the context implements some custom interface
        // to pass messsage back
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
