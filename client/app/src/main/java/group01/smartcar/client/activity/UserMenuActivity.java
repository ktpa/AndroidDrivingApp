package group01.smartcar.client.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ScheduledFuture;

import group01.smartcar.client.R;
import group01.smartcar.client.async.TaskExecutor;

public class UserMenuActivity extends AppCompatActivity {
    // Battery monitor adapted from https://www.youtube.com/watch?v=GxfdnOtRibQ&ab_channel=TihomirRAdeff

    private Handler handler;
    private Runnable runnable;
    private TextView batteryText;
    private ImageView batteryImage;

    private ScheduledFuture<?> batteryRenderer;

    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_usermenu);

        batteryText = findViewById(R.id.battery_text);
        batteryImage = findViewById(R.id.battery_image);

        ((TextView) findViewById(R.id.username_field)).setText(firebaseUser != null
            ? firebaseUser.getEmail()
            : "DEBUG MODE"
        );

        hideSystemUI();

        registerComponentCallbacks();

        batteryRenderer = TaskExecutor.getInstance().scheduleTask(this::renderBatteryLevel, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (batteryRenderer != null && batteryRenderer.isCancelled()) {
            batteryRenderer = TaskExecutor.getInstance().scheduleTask(this::renderBatteryLevel, 5000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!batteryRenderer.isCancelled()) {
            batteryRenderer.cancel(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!batteryRenderer.isCancelled()) {
            batteryRenderer.cancel(true);
        }
    }

    @SuppressLint("SetTextI18n")
    private void registerComponentCallbacks() {
        findViewById(R.id.logout_button).setOnClickListener(this::onLogoutButtonClick);
        findViewById(R.id.drive_alset_button).setOnClickListener(this::onDriveButtonClick);

    }

    private float getBatteryLevel() {
        final Intent batteryIntent = registerReceiver(
            null,
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );

        final int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        final int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (level == -1 || scale == -1) {
            return 50.0f;
        }

        return (float) level / (float) scale * 100.0f;
    }

    @SuppressLint("SetTextI18n")
    private void renderBatteryLevel() {
        int level = (int) getBatteryLevel();
        batteryText.setText(level + "%");

        System.out.println(level);

        if (level > 75) {
            batteryImage.setImageResource(R.drawable.battery_full);
        }

        if (level > 50 && level <= 75) {
            batteryImage.setImageResource(R.drawable.battery_high);
        }

        if (level > 25 && level <= 50) {
            batteryImage.setImageResource(R.drawable.battery_medium);
        }

        if (level <= 25) {
            batteryImage.setImageResource(R.drawable.battery_low);
        }
    }

    private void onLogoutButtonClick(View view) {
        // TODO: Actually log out and pass Toast to next screen
        FirebaseAuth.getInstance().signOut();

        final Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void onDriveButtonClick(View view) {
        final Intent intent = new Intent(this, DrivingActivity.class);
        startActivity(intent);
        finish();
    }

    private void hideSystemUI() {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }
}
