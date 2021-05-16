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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import group01.smartcar.client.R;
import group01.smartcar.client.SmartCarApplication;

public class UserMenuActivity extends AppCompatActivity {
    // Battery monitor adapted from https://www.youtube.com/watch?v=GxfdnOtRibQ&ab_channel=TihomirRAdeff

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance
            ("https://smartcar-client-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseReference = firebaseDatabase.getReference();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Handler handler;
    private Runnable runnable;
    private TextView batteryText;
    private ImageView batteryImage;
    private SeekBar seekBar;
    private Toast toast;

    private final Map<String, Object> savedUserSession = new HashMap<>();

    private ScheduledFuture<?> batteryRenderer;

    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    @SuppressLint({"SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usermenu);

        seekBar = findViewById(R.id.driving_sensitivity);

        if (firebaseUser != null) {
            databaseReference.child("sensitivity").get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(
                            UserMenuActivity.this,
                            "Error getting data, setting to default sensitivity",
                            Toast.LENGTH_SHORT);
                    toast.show();
                    savedUserSession.put("sensitivity", calculateSensitivity(5));
                    seekBar.setProgress(convertSensitivityToSeekBar((Float) savedUserSession.get("sensitivity")));
                } else {
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Number sensitivity = 1.0;
                            try {
                                sensitivity = (Number) snapshot.child
                                        ("users/" + firebaseUser.getUid() + "/sensitivity").getValue();
                            } catch (NullPointerException ignored) {

                            }

                            if (sensitivity == null) {
                                sensitivity = 1.0;
                            }

                            seekBar.setProgress((int) Math.round(sensitivity.doubleValue() * 5));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        } else {
            savedLocalSeekBar();
        }

        onSeekBarChange();

        ((TextView) findViewById(R.id.username_field)).setText(firebaseUser != null
                ? firebaseUser.getEmail()
                : "DEBUG MODE"
        );

        batteryText = findViewById(R.id.battery_text);
        batteryImage = findViewById(R.id.battery_image);

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
                if (firebaseUser != null) {
                    savedUserSession.put("sensitivity", calculateSensitivity(progress));
                    databaseReference.child
                            ("users/" + firebaseUser.getUid())
                            .updateChildren(savedUserSession);
                } else {
                    sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.putFloat("sensitivity", calculateSensitivity(progress));
                    editor.apply();
                }

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

    // Returns the sensitivity that is passed onto the joystick
    private float calculateSensitivity(int progress) {
        return (float) progress / ((float) seekBar.getMax() / 2);
    }

    /* Converts the sensitivity passed onto the joystick into the appropriate value for toolbar,
       e.g. sensitivity of 2 is equal to 10 in the toolbar */
    private int convertSensitivityToSeekBar(float sensitivity) {
        return (int) (sensitivity * (seekBar.getMax() / 2));
    }

    private void onLogoutButtonClick(View view) {
        // TODO: Actually log out and pass Toast to next screen
        savedUserSession.put("isLoggedIn", "");
        databaseReference.child
                ("users/" + firebaseUser.getUid())
                .updateChildren(savedUserSession);
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

    private void savedLocalSeekBar() {
        sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        if (!sharedPreferences.contains("sensitivity")) {
            editor = sharedPreferences.edit();
            editor.putFloat("sensitivity", calculateSensitivity(5));
            editor.apply();
        }

        seekBar.setProgress(convertSensitivityToSeekBar(sharedPreferences.getFloat
                ("sensitivity", calculateSensitivity(5))));
    }
}
