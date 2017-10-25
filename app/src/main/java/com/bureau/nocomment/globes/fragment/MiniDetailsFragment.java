package com.bureau.nocomment.globes.fragment;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.model.ModelRepository;
import com.bureau.nocomment.globes.model.Project;

import java.io.IOException;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MiniDetailsFragment extends BaseFragment {

    private final static String SOUND_FOLDER = "sound";

    @Bind(R.id.progress)
    CircleProgressView progressView;

    @Bind(R.id.play_button)
    ImageButton playButton;

    @Bind(R.id.pause_button)
    ImageButton pauseButton;

    MediaPlayer                  player;
    private Handler              progressUpdateHandler;
    private Runnable             progressUpdater;

    public void playProject(int projectID) {
        Project project = ModelRepository.getInstance().getItemLibrary().findProject(projectID);
        loadFromProject(project);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_minidetails, container, false);
        ButterKnife.bind(this, rootView);

        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                pauseSoundtrack();
                player.seekTo(0);
                progressView.setValueAnimated(0, 800);
            }
        });

        progressUpdateHandler = new Handler();
        progressUpdater = createUpdater();

        progressView.setOnProgressManualChangeListener(new CircleProgressView.OnProgressManualChangeListener() {
            @Override
            public void onUserDidChangeProgress(float value) {
                if(player != null){
                    player.seekTo((int)value);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // maybe check whether the context implements some custom interface
        // to pass messsage back
    }

    @Override
    public void onResume() {
        super.onResume();
        playSoundtrack();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseSoundtrack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        progressUpdateHandler.removeCallbacks(progressUpdater);
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void playSoundtrack() {
        progressUpdateHandler.post(progressUpdater);
        player.start();
        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    private void pauseSoundtrack() {
        progressUpdateHandler.removeCallbacks(progressUpdater);
        player.pause();
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

    private Runnable createUpdater() {
        return new Runnable() {
            @Override
            public void run() {
                if(player != null){
                    int mCurrentPosition = player.getCurrentPosition();
                    progressView.setValue((float)mCurrentPosition);
                }
                progressUpdateHandler.postDelayed(this, 1000);
            }
        };
    }

    private void loadFromProject(Project project) {
        loadAudioAsset(project.getAudioFile());
        // update progress bar
        progressView.setMaxValue(player.getDuration()); // in ms
    }

    private void loadAudioAsset(String audioFile) {
        AssetFileDescriptor descriptor = null;
        try {
            descriptor = getContext().getAssets().openFd(SOUND_FOLDER + "/" + audioFile);
            if (descriptor != null) {
                long offset = descriptor.getStartOffset();
                long length = descriptor.getLength();
                player.reset();
                player.setDataSource(descriptor.getFileDescriptor(), offset, length);
                player.prepare();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (descriptor != null) {
                    descriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
