package com.bureau.nocomment.globes.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewTreeObserver;


abstract public class BaseFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                onViewDrawn();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Add tagging later
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add tagging later
    }

    /**
     * Called when the view activity is actually drawn for the first time. Activity views sizes are
     * then correctly initialized.
     */
    protected void onViewDrawn() {
        // Add tagging later
    }

    @Override
    public void onPause() {
        super.onPause();
        // Add tagging later
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Add tagging later
    }

}
