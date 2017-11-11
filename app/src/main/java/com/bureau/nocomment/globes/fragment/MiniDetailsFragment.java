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
import android.widget.TextView;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.model.ModelRepository;
import com.bureau.nocomment.globes.model.Project;
import com.bureau.nocomment.globes.model.Table;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MiniDetailsFragment extends BaseFragment {

    interface PlayerListener {
        void onReadyToPlay();
        void playerDidStartToPlay(int trackId);
        void playerDidEndToPlay(int trackId);
    }

    private final static String SOUND_FOLDER = "sound";

    @Bind(R.id.progress)
    CircleProgressView progressView;

    @Bind(R.id.play_button)
    ImageButton playButton;

    @Bind(R.id.pause_button)
    ImageButton pauseButton;

    @Bind(R.id.project_number)
    TextView projectNumber;

    @Bind(R.id.title)
    TextView title;

    @Bind(R.id.description)
    TextView description;

    // to avoid loading issues with multiple taps
    private int currentTableId   = -1;
    private int currentProjectId = -1;
    private int loadedTrackId    = -1; // a table id

    MediaPlayer                  player;
    private Handler              progressUpdateHandler;
    private Runnable             progressUpdater;

    PlayerListener               mPlayerListener;

    Boolean                      mTrackCompletable;

    public void showProject(int projectID) {
        Project project = ModelRepository.getInstance().getItemLibrary().findProject(projectID);
        loadFromProject(project);
    }

    public void showTable(int tableID, boolean playSound) {
        Table table = ModelRepository.getInstance().getItemLibrary().findTable(tableID);
        loadFromTable(table);
        if (playSound) {
            playSoundtrack();
        }
    }

    public void setPlayerListener(PlayerListener playerListener) {
        mPlayerListener = playerListener;
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
                if (!mTrackCompletable) {
                    return;
                }
                pauseSoundtrack();
                player.seekTo(0);
                progressView.setValueAnimated(0, 800);
                if (mPlayerListener != null) {
                    mPlayerListener.playerDidEndToPlay(loadedTrackId);
                }
                mTrackCompletable = false;
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
        if (mPlayerListener != null) {
            mPlayerListener.onReadyToPlay();
        }
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
        if (mPlayerListener != null) {
            mPlayerListener.playerDidStartToPlay(loadedTrackId);
        }
        mTrackCompletable = true;
    }

    private void pauseSoundtrack() {
        progressUpdateHandler.removeCallbacks(progressUpdater);
        if (loadedTrackId > 0 && player.isPlaying()) {
            player.pause();
        }
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
    }

    private void stopSoundtrack() {
        pauseSoundtrack();
        player.stop();
        progressView.setValueAnimated(0, 400);
        if (mPlayerListener != null) {
            mPlayerListener.playerDidEndToPlay(loadedTrackId);
        }
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
        if (currentProjectId == project.getId()) {
            return;
        }
        // update progress bar

        currentProjectId = project.getId();
        currentTableId = -1;

        String number = String.format(Locale.getDefault(), "%d", project.getId());
        projectNumber.setText(number);

        description.setVisibility(View.VISIBLE);
        configureProjectDetails(project);

        progressView.setVisibility(View.INVISIBLE);
        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.GONE);
        projectNumber.setVisibility(View.VISIBLE);
    }

    private void configureProjectDetails(Project project) {
        List<Project> projects = ModelRepository.getInstance().getItemLibrary().projectsHavingId(project.getId());
        String projectTitle = project.getName();
        String projectDescription = project.getAuthor();
        String previousAuthor = projectDescription;
        if (projects.size() > 1) {
            for (int i = 1; i < projects.size(); ++i) {
                Project p = projects.get(i);
                projectTitle = projectTitle + " ; " + p.getName();
                if (!p.getAuthor().equals(previousAuthor)) {
                    projectDescription = projectDescription + " ; " + p.getAuthor();
                    previousAuthor = p.getAuthor();
                }
            }
        }
        title.setText(projectTitle);
        description.setText(projectDescription);
        // HBR FIXME Whattttt a hack ; use autoresizing textView instead
        description.setTextSize((projectDescription.length() > 80) ? 12 : 14);
    }

    private void loadFromTable(Table table) {
        if (currentTableId == table.getId()) {
            return;
        }
        if (loadedTrackId != table.getId()) {
            loadAudioAsset(table.getAudioFile());
            // update progress bar
            progressView.setMaxValue(player.getDuration()); // in ms
            loadedTrackId = table.getId();
        }

        currentTableId = table.getId();
        currentProjectId = -1;

        description.setVisibility(View.GONE);
        title.setText(table.getTitle());

        progressView.setVisibility(View.VISIBLE);
        projectNumber.setVisibility(View.INVISIBLE);

        if (player.isPlaying()) {
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        } else {
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        }
    }

    private void loadAudioAsset(String audioFile) {
        AssetFileDescriptor descriptor = null;
        try {
            descriptor = getContext().getAssets().openFd(SOUND_FOLDER + "/" + audioFile);
            if (descriptor != null) {
                long offset = descriptor.getStartOffset();
                long length = descriptor.getLength();
                if (loadedTrackId > 0) {
                    stopSoundtrack();
                }
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
