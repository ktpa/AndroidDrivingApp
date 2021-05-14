package group01.smartcar.client.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.ScheduledFuture;

import group01.smartcar.client.R;
import group01.smartcar.client.SmartCarApplication;

public class UserMenuActivity extends AppCompatActivity {
    // Battery monitor adapted from https://www.youtube.com/watch?v=GxfdnOtRibQ&ab_channel=TihomirRAdeff

    private FirebaseDatabase firebaseDatabase;
    private Handler handler;
    private Runnable runnable;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private TextView batteryText;
    private ImageView batteryImage;
    private SeekBar seekBar;
    private Toast toast;

    private ScheduledFuture<?> batteryRenderer;

    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    @SuppressLint({"SetTextI18n", "ApplySharedPref"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usermenu);

        sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putFloat("sensitivity", 1);
        editor.commit();

        batteryText = findViewById(R.id.battery_text);
        batteryImage = findViewById(R.id.battery_image);

        seekBar = findViewById(R.id.driving_sensitivity);
        onSeekBarChange();

        ((TextView) findViewById(R.id.username_field)).setText(firebaseUser != null
            ? firebaseUser.getEmail()
            : "DEBUG MODE"
        );

        hideSystemUI();

        registerComponentCallbacks();

        batteryRenderer = SmartCarApplication.getTaskExecutor().scheduleTask(this::renderBatteryLevel, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (batteryRenderer != null && batteryRenderer.isCancelled()) {
            batteryRenderer = SmartCarApplication.getTaskExecutor().scheduleTask(this::renderBatteryLevel, 5000);
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
        findViewById(R.id.drive_button).setOnClickListener(this::onDriveButtonClick);
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

    private void onSeekBarChange() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putFloat("sensitivity", calculateSensitivity(progress));
                editor.commit();

                if (toast != null) {
                    toast.cancel();
                }

                toast = Toast.makeText(
                        UserMenuActivity.this,
                        "Driving sensitivity: " + progress + "/10",
                        Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private float calculateSensitivity(int progress) {
        return progress / ((float) seekBar.getMax() / 2);
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
