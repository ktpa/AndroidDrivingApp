package group01.smartcar.client;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static group01.smartcar.client.Status.*;
import static java.util.Objects.requireNonNull;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    EditText emailTextView, passwordTextView;
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

        firebaseAuth = FirebaseAuth.getInstance();
        registerComponentCallbacks();
    }

    private void registerComponentCallbacks() {
        emailTextView = findViewById(R.id.email_textfield);
        passwordTextView = findViewById(R.id.password_textfield);
        findViewById(R.id.login_button).setOnClickListener(login);
    }

    private final View.OnClickListener login = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, UserMenuActivity.class);
            MainActivity.this.startActivityForResult(intent, 0);
            Toast.makeText(getApplicationContext(), "Welcome to AlSet", Toast.LENGTH_SHORT)
                    .show();
            finish();
            /*
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
                                MainActivity.this, "Failed to log in! Ask your " +
                                        "local AlSet dealer for your " +
                                        "personal login details.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
             */
        }
    };

    private void verifyUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(MainActivity.this, UserMenuActivity.class);
            MainActivity.this.startActivityForResult(intent, 0);
            Toast.makeText(getApplicationContext(), "Welcome to AlSet", Toast.LENGTH_SHORT)
                    .show();
            finish();
        } else {
            Toast.makeText(MainActivity.this, "Your login details are incorrect.",
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