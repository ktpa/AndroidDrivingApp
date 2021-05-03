package group01.smartcar.client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserMenuActivity extends AppCompatActivity {
    // Battery monitor adapted from https://www.youtube.com/watch?v=GxfdnOtRibQ&ab_channel=TihomirRAdeff

    private Handler handler;
    private Runnable runnable;
    private TextView batteryText;
    private ImageView batteryImage;

    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usermenu);
        hideSystemUI();
        registerComponentCallbacks();

        runnable = () -> {
            int level = (int) batteryLevel();
            batteryText.setText(level + "%");

            if (level > 75)
                batteryImage.setImageResource(R.drawable.battery_full);

            if (level > 50 && level <= 75)
                batteryImage.setImageResource(R.drawable.battery_high);

            if (level > 25 && level <= 50)
                batteryImage.setImageResource(R.drawable.battery_medium);

            if (level >= 0 && level <= 25)
                batteryImage.setImageResource(R.drawable.battery_low);

            handler.postDelayed(runnable, 1000);
        };

        handler = new Handler();
        handler.postDelayed(runnable, 0);
    }

    @SuppressLint("SetTextI18n")
    private void registerComponentCallbacks() {
        TextView usernameView = findViewById(R.id.username_field);

        if (firebaseUser != null)
            usernameView.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        else
            usernameView.setText("DEBUG MODE");

        findViewById(R.id.logout_button).setOnClickListener(this::onLogoutButtonClick);
        findViewById(R.id.drive_alset_button).setOnClickListener(this::onDriveButtonClick);
        batteryText = (TextView) findViewById(R.id.battery_text);
        batteryImage = (ImageView) findViewById(R.id.battery_image);
    }

    private float batteryLevel() {
        Intent batteryIntent = registerReceiver(
                null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (level == -1 || scale == -1) {
            return 50.0f;
        }
        return (float) level / (float) scale * 100.0f;
    }

    private void onLogoutButtonClick(View view) {
        // TODO: Actually log out and pass Toast to next screen
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void onDriveButtonClick(View view) {
        Intent intent = new Intent(this, DrivingScreen.class);
        startActivity(intent);
        finish();
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
