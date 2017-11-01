package com.bureau.nocomment.globes.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.model.ModelRepository;
import com.bureau.nocomment.globes.model.Project;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.IOException;
import java.io.InputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DetailActivity extends AppCompatActivity {

    private final static String IMAGE_FOLDER = "images";
    private final static String SOUND_FOLDER = "sound";

    @Bind(R.id.item_image)
    PhotoView itemImage;

    @Bind(R.id.item_title)
    TextView         itemTitle;

    @Bind(R.id.item_subtitle)
    TextView         itemSubtitle;

    @Bind(R.id.item_description)
    TextView         itemDescription;

    @Bind(R.id.play_button)
    ImageButton      playButton;

    @Bind(R.id.pause_button)
    ImageButton      pauseButton;

    @Bind(R.id.progress_bar)
    AppCompatSeekBar progressBar;

    MediaPlayer player;
    private Handler  progressUpdateHandler;
    private Runnable progressUpdater;

    private static final String TEST_PROJECT_ID_EXTRA =
            DetailActivity.class.getCanonicalName().concat("project_id");

    public static Intent makeTestIntent(Context context, int projectId) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(TEST_PROJECT_ID_EXTRA, projectId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                pauseSoundtrack();
                player.seekTo(0);
            }
        });

        itemImage.setMaximumScale(10);
        actionBar.setTitle("Théâtre sphérique");
        itemTitle.setVisibility(View.GONE);

        itemDescription.setMovementMethod(new ScrollingMovementMethod());

        progressUpdateHandler = new Handler();
        progressUpdater = createUpdater();
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(player != null && fromUser){
                    player.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressUpdateHandler.removeCallbacks(progressUpdater);
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playSoundtrack();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseSoundtrack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                    progressBar.setProgress(mCurrentPosition);
                }
                progressUpdateHandler.postDelayed(this, 1000);
            }
        };
    }

    // To workaround a Samsung bug
    private static CharSequence italicCharSequenceFrom(CharSequence text) {
        final StyleSpan style = new StyleSpan(Typeface.ITALIC);
        final SpannableString str = new SpannableString(text);
        str.setSpan(style, 0, text.length(), 0);
        return str;
    }

    private void loadFromProjectId(int testProjectId) {
        Project project = ModelRepository.getInstance().getItemLibrary().findProject(testProjectId);

        if (project.getImages().size() > 0) {
            String imageName = project.getImages().get(0); // TODO : handle more than one pic
            Drawable drawable = loadImageAsset(imageName);
            itemImage.setImageDrawable(drawable);
        }

        itemSubtitle.setText(italicCharSequenceFrom(project.getSubtitle()));

        // TODO : faudra peut-être songer à trouver mieux que ce hack '\r\n'
        itemDescription.setText(project.getDescription() + "\r\n" + "\r\n" + "\r\n" + "\r\n" + "\r\n");

        loadAudioAsset(project.getAudioFile());
        // update progress bar
        progressBar.setMax(player.getDuration()); // in ms
    }

    private void loadAudioAsset(String audioFile) {
        AssetFileDescriptor descriptor = null;
        try {
            descriptor = getAssets().openFd(SOUND_FOLDER + "/" + audioFile);
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

    private Drawable loadImageAsset(String filename) {
        InputStream inputStream = null;
        Drawable drawable = null;
        // TODO : this try/catch/finally/try/if/catch skeletton is common to loadImage and loadAudio
        // => try to leverage if (kinda closure) to leverage the common Closeable interface
        try {
            inputStream = getAssets().open(IMAGE_FOLDER + "/" + filename);
            drawable = Drawable.createFromStream(inputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return drawable;
    }
}
