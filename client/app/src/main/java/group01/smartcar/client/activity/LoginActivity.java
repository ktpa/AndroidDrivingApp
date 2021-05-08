package group01.smartcar.client.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import group01.smartcar.client.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText emailTextView, passwordTextView;
    private VideoView videoBackground;
    private MediaPlayer mediaPlayer;
    private int currentVideoPosition;

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

        firebaseAuth = FirebaseAuth.getInstance();
        registerComponentCallbacks();
    }

    private void onDebugModeActivated(View view) {
        Intent intent = new Intent(this, UserMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void registerComponentCallbacks() {
        emailTextView = findViewById(R.id.email_textfield);
        passwordTextView = findViewById(R.id.password_textfield);
        findViewById(R.id.login_button).setOnClickListener(login);
        findViewById(R.id.debug_mode).setOnClickListener(this::onDebugModeActivated);
    }

    private final View.OnClickListener login = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = emailTextView.getText().toString();
            String password = passwordTextView.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("EmailPassword", "signInWithEmail:success");
                        verifyUser();
                    } else {
                        Log.w("EmailPassword", "signInWithEmail:failure",
                                task.getException());
                        Toast.makeText(
                                LoginActivity.this, "Failed to log in! Ask your " +
                                        "local AlSet dealer for your " +
                                        "personal login details.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    private void verifyUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(LoginActivity.this, UserMenuActivity.class);
            LoginActivity.this.startActivityForResult(intent, 0);
            Toast.makeText(getApplicationContext(), "Welcome to AlSet", Toast.LENGTH_SHORT)
                    .show();
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Your login details are incorrect.",
                    Toast.LENGTH_SHORT).show();
        }
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed(0.5f);
                mediaPlayer.setPlaybackParams(playbackParams);
            }

            if (currentVideoPosition != 0) {
                mediaPlayer.seekTo(currentVideoPosition);
                mediaPlayer.start();
            }
        });
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