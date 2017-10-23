package com.bureau.nocomment.globes.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bureau.nocomment.globes.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MiniDetailsFragment extends BaseFragment {

    @Bind(R.id.play_button)
    ImageButton playButton;

    @Bind(R.id.pause_button)
    ImageButton pauseButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_minidetails, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
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

    private void playSoundtrack() {
//        progressUpdateHandler.post(progressUpdater);
//        player.start();
        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    private void pauseSoundtrack() {
//        progressUpdateHandler.removeCallbacks(progressUpdater);
//        player.pause();
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
    }

    @OnClick(R.id.play_button)
    void onPlayButton(ImageButton button) {
        playSoundtrack();
    }

    @OnClick(R.id.pause_button)
    void onPauseButton(ImageButton button) {
        pauseSoundtrack();
    }
}
