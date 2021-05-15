package group01.smartcar.client.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import group01.smartcar.client.R;

public class LoginActivity extends AppCompatActivity {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance
            ("https://smartcar-client-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseReference = firebaseDatabase.getReference();
    private final Map<String, Object> userMap = new HashMap<>();

    private EditText emailTextView;
    private EditText passwordTextView;

    private VideoView videoBackground;
    private MediaPlayer mediaPlayer;
    private Toast loginToast;

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
                cancelToast(loginToast);
                loginToast = Toast.makeText(
                        LoginActivity.this,
                        "Failed to log in! Ask your " +
                                "local AlSet dealer for your " +
                                "personal login details.",
                        Toast.LENGTH_SHORT);
                loginToast.show();

                return;
            }
            // Check if a user is already logged into the account
            databaseReference.child("users/" + firebaseAuth.getCurrentUser().getUid() + "/isLoggedIn")
                    .get().addOnCompleteListener(dbTask -> {
                if (!dbTask.isSuccessful()) {
                    Log.e("ERROR", "Error getting data");
                } else {
                    Object userSession = Boolean.FALSE;
                    if (dbTask.getResult().exists()) {
                        userSession = dbTask.getResult().getValue();
                    }

                    if (userSession.equals(true)) {
                        cancelToast(loginToast);
                        loginToast = Toast.makeText(getApplicationContext(),
                            "You cannot log in on multiple devices! " +
                                    "Log out of your initial session.",
                            Toast.LENGTH_SHORT);
                        loginToast.show();
                    } else {
                        userMap.put("email", Objects.requireNonNull(firebaseAuth.getCurrentUser())
                                .getEmail());
                        userMap.put("isLoggedIn", true);

                        databaseReference.child("users/" + firebaseAuth.getCurrentUser().getUid())
                                .updateChildren(userMap);

                        final Intent intent = new Intent(
                                LoginActivity.this,
                                UserMenuActivity.class);

                        LoginActivity.this.startActivityForResult(intent, 0);

                        cancelToast(loginToast);
                        loginToast = Toast.makeText(getApplicationContext(),
                                "Welcome to AlSet",
                                Toast.LENGTH_SHORT);
                        loginToast.show();

                        finish();
                    }
                }
            });
        });
    }


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

            final PlaybackParams playbackParams = new PlaybackParams();
            playbackParams.setSpeed(0.5f);
            mediaPlayer.setPlaybackParams(playbackParams);

            if (currentVideoPosition != 0) {
                mediaPlayer.seekTo(currentVideoPosition);
                mediaPlayer.start();
            }
        });
    }

    private void cancelToast(Toast toast) {
        if (toast != null) {
            toast.cancel();
        }
    }

    private void onDebugModeActivated(View view) {
        final Intent intent = new Intent(this, UserMenuActivity.class);
        startActivity(intent);
        finish();
    }
}