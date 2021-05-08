package group01.smartcar.client.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import group01.smartcar.client.R;

public class LoginActivity extends AppCompatActivity {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private EditText emailTextView;
    private EditText passwordTextView;

    private VideoView videoBackground;
    private MediaPlayer mediaPlayer;

    private int currentVideoPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

        emailTextView = findViewById(R.id.email_textfield);
        passwordTextView = findViewById(R.id.password_textfield);

        loadBackground();

        registerComponentCallbacks();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadBackground();
    }

    @Override
    protected void onPause() {
        super.onPause();

        currentVideoPosition = mediaPlayer.getCurrentPosition();

        videoBackground.pause();
        mediaPlayer.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mediaPlayer.release();
    }

    private void registerComponentCallbacks() {
        findViewById(R.id.login_button).setOnClickListener(this::onLoginClicked);
        findViewById(R.id.debug_mode).setOnClickListener(this::onDebugModeActivated);
    }

    private void onLoginClicked(View view) {
        final String email = emailTextView.getText().toString();
        final String password = passwordTextView.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(
                    LoginActivity.this,
                    "Failed to log in! Ask your " +
                            "local AlSet dealer for your " +
                            "personal login details.",
                    Toast.LENGTH_SHORT).show();

                return;
            }

            final Intent intent = new Intent(LoginActivity.this, UserMenuActivity.class);
            LoginActivity.this.startActivityForResult(intent, 0);

            Toast.makeText(getApplicationContext(), "Welcome to AlSet", Toast.LENGTH_SHORT).show();

            finish();
        });
    };

    private void loadBackground() {
        videoBackground = findViewById(R.id.videoView);
        videoBackground.getHolder().setSizeFromLayout();

        final Uri uri = Uri.parse("android.resource://"
            + getPackageName()
            + "/"
            + R.raw.background
        );

        videoBackground.setVideoURI(uri);
        videoBackground.start();

        videoBackground.setOnPreparedListener((mediaPlayer) -> {
            this.mediaPlayer = mediaPlayer;
            mediaPlayer.setLooping(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed(0.5f);
                mediaPlayer.setPlaybackParams(playbackParams);
            }

            if (currentVideoPosition != 0) {
                mediaPlayer.seekTo(currentVideoPosition);
                mediaPlayer.start();
            }
        });
    }

    private void onDebugModeActivated(View view) {
        final Intent intent = new Intent(this, UserMenuActivity.class);
        startActivity(intent);
        finish();
    }
}