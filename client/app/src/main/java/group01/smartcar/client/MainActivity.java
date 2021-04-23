package group01.smartcar.client;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import static group01.smartcar.client.Status.*;

public class MainActivity extends AppCompatActivity {
    private VideoView videoBackground;
    MediaPlayer mediaPlayer;
    int currentVideoPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        videoBackground = findViewById(R.id.videoView);
        videoBackground.getHolder().setSizeFromLayout();
        loadBackground();
        registerComponentCallbacks();
    }

    private void loadBackground() {
        videoBackground = findViewById(R.id.videoView);

        Uri uri = Uri.parse("android.resource://"
                + getPackageName()
                + "/"
                + R.raw.background);

        videoBackground.setVideoURI(uri);
        videoBackground.start();

        videoBackground.setOnPreparedListener((mediaPlayer) -> {
            this.mediaPlayer = mediaPlayer;
            mediaPlayer.setLooping(true);

            if (currentVideoPosition != 0) {
                mediaPlayer.seekTo(currentVideoPosition);
                mediaPlayer.start();
            }
        });
    }

    private void registerComponentCallbacks() {
        findViewById(R.id.login_button).setOnClickListener(this::onLoginButtonClick);
    }

    private void onLoginButtonClick(View view) {
        setContentView(R.layout.activity_drive);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.currentVideoPosition = mediaPlayer.getCurrentPosition();
        videoBackground.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoBackground.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        this.mediaPlayer = null;
    }
}