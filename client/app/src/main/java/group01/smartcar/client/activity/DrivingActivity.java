package group01.smartcar.client.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import group01.smartcar.client.R;
import group01.smartcar.client.SmartCar;
import group01.smartcar.client.SmartCarApplication;
import group01.smartcar.client.SmartCarVoiceControl;
import group01.smartcar.client.speech.SpeechListener;
import group01.smartcar.client.view.ProximityIndicator;
import group01.smartcar.client.view.Speedometer;

import static group01.smartcar.client.SmartCar.Status.ACTIVE;

// 78 to 112 adapted from https://developer.android.com/training/system-ui/immersive .

public class DrivingActivity extends AppCompatActivity {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance
            ("https://smartcar-client-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseReference = firebaseDatabase.getReference();
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    private static final Integer RECORD_AUDIO_REQUEST_CODE = 1;

    private View joystick;
    private SpringAnimation animJoystickY;
    private SpringAnimation animJoystickX;
    private final float joystickRadiusMax = 180;
    private float joystickInitX;
    private float joystickInitY;
    private boolean notSetInit;
    private float joystickRadius;
    private float drivingSensitivity = 1;

    private SmartCar car;
    private ImageView cameraView;
    private Speedometer speedometer;
    private ProximityIndicator proximityIndicator;
    private Vibrator vibrator;
    private ImageView micButton;
    private SpeechListener speechListener;
    private ImageView batteryImage;
    private ImageView backButton;

    private SmartCarVoiceControl voiceControl;

    private ScheduledFuture<?> speedometerUpdater;
    private ScheduledFuture<?> proximitySensorUpdater;
    private ScheduledFuture<?> batteryRenderer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        requestRequiredPermissions();

        if (firebaseUser == null) {
            fetchSensitivityLocally();
        } else {
            fetchSensitivityFromDatabase();
        }

        cameraView = findViewById(R.id.imageView);
        speedometer = findViewById(R.id.fancySpeedometer);
        proximityIndicator = findViewById(R.id.proximitySensor);
        micButton = findViewById(R.id.micButton);
        joystick = findViewById(R.id.joystick);
        batteryImage = findViewById(R.id.battery_image);
        backButton = findViewById(R.id.backbtn);

        animJoystickY = new SpringAnimation(joystick, DynamicAnimation.TRANSLATION_Y, 0);
        animJoystickX = new SpringAnimation(joystick, DynamicAnimation.TRANSLATION_X, 0);
        notSetInit = true;
        joystickRadius = 0;

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        car = SmartCar.createCar(getApplicationContext());
        car.onCameraFrameReceived(this::onCameraFrameReceived);
        car.onSpeedUpdated(speedometer::setCurrentSpeedMS);
        car.onMotorPowerUpdated(speedometer::setMotorPowerPercentage);

        voiceControl = SmartCarVoiceControl.create(car);

        speechListener = new SpeechListener(this);
        speechListener.onResults(this::onSpeechResults);

        registerComponentCallbacks();

        speedometerUpdater = SmartCarApplication.getTaskExecutor().scheduleTask(speedometer::update);
        proximitySensorUpdater = SmartCarApplication.getTaskExecutor().scheduleTask(proximityIndicator::update);
        batteryRenderer = SmartCarApplication.getTaskExecutor().scheduleTask(this::renderBatteryLevel, 5000);

        speedometer.setVisibility(View.INVISIBLE);
        proximityIndicator.setVisibility(View.INVISIBLE);
        cameraView.setVisibility(View.INVISIBLE);
        joystick.setVisibility(View.INVISIBLE);
        micButton.setVisibility(View.INVISIBLE);
    }


    @Override
    protected void onResume() {
        super.onResume();

        car.resume();

        if (speedometerUpdater != null && speedometerUpdater.isCancelled()) {
            speedometerUpdater = SmartCarApplication.getTaskExecutor().scheduleTask(speedometer::update);
        }

        if (proximitySensorUpdater != null && proximitySensorUpdater.isCancelled()) {
            proximitySensorUpdater = SmartCarApplication.getTaskExecutor().scheduleTask(proximityIndicator::update);
        }

        if (batteryRenderer != null && batteryRenderer.isCancelled()) {
            batteryRenderer = SmartCarApplication.getTaskExecutor().scheduleTask(this::renderBatteryLevel, 5000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        car.pause();

        if (!speedometerUpdater.isCancelled()) {
            speedometerUpdater.cancel(true);
        }

        if (!proximitySensorUpdater.isCancelled()) {
            proximitySensorUpdater.cancel(true);
        }

        if (!batteryRenderer.isCancelled()) {
            batteryRenderer.cancel(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        speechListener.destroy();

        if (!speedometerUpdater.isCancelled()) {
            speedometerUpdater.cancel(true);
        }

        if (!proximitySensorUpdater.isCancelled()) {
            proximitySensorUpdater.cancel(true);
        }

        if (!batteryRenderer.isCancelled()) {
            batteryRenderer.cancel(true);
        }
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

    @SuppressLint("ClickableViewAccessibility")
    private void registerComponentCallbacks() {
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        final Switch sw = findViewById(R.id.drive_park_switch);
        findViewById(R.id.backbtn).setOnClickListener(this::onBackbtnClicked);


        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final VibrationEffect vibrationEffect1;
            final Drawable thumb = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_switch_thumb, null);
            final Drawable thumbActive = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_switch_thumb_active, null);
            final Drawable track = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_switch_track, null);
            final Drawable trackActive = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_switch_track_active, null);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrationEffect1 = VibrationEffect.createOneShot(100, VibrationEffect.EFFECT_HEAVY_CLICK);
                vibrator.cancel();
                vibrator.vibrate(vibrationEffect1);
            } else {
                vibrator.vibrate(100);
            }

            if(isChecked) {
                car.start();
                sw.setThumbDrawable(thumbActive);
                sw.setTrackDrawable(trackActive);
                speedometer.setVisibility(View.VISIBLE);
                proximityIndicator.setVisibility(View.VISIBLE);
                cameraView.setVisibility(View.VISIBLE);
                joystick.setVisibility(View.VISIBLE);
                micButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.INVISIBLE);
            } else {
                car.stop();
                sw.setThumbDrawable(thumb);
                sw.setTrackDrawable(track);
                speedometer.setVisibility(View.INVISIBLE);
                proximityIndicator.setVisibility(View.INVISIBLE);
                cameraView.setVisibility(View.INVISIBLE);
                joystick.setVisibility(View.INVISIBLE);
                micButton.setVisibility(View.INVISIBLE);
                backButton.setVisibility(View.VISIBLE);
            }
        });

        micButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                micButton.setImageResource(R.drawable.ic_mic);
                speechListener.stop();
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                micButton.setImageResource(R.drawable.ic_mic_active);
                speechListener.start();
            }

            return false;
        });

        joystick.setOnTouchListener((view, motionEvent) -> {
            if (notSetInit) {
                setInit();
                notSetInit = false;
            }

            float touchDistance;
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                touchDistance = (float) Math.sqrt(Math.pow(motionEvent.getRawX() - joystickInitX - joystick.getWidth() / 2f, 2) + Math.pow(motionEvent.getRawY() - joystickInitY - joystick.getHeight() / 2f, 2));
                if (touchDistance <= joystickRadiusMax) {
                    joystick.setX(motionEvent.getRawX() - joystickRadius);
                    joystick.setY(motionEvent.getRawY() - joystickRadius);
                } else {
                    joystick.setX((((motionEvent.getRawX() - joystickRadius) - joystick.getLeft()) * joystickRadiusMax/touchDistance) + joystick.getLeft());
                    joystick.setY((((motionEvent.getRawY() - joystickRadius) - joystick.getTop()) * joystickRadiusMax/touchDistance) + joystick.getTop());
                }
                    onJoystickMoved(drivingSensitivity,(joystick.getX() - joystickInitX) / joystickRadius, (joystick.getY() - joystickInitY) / joystickRadius, joystick.getId());
                    return true;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    animJoystickX.start();
                    animJoystickY.start();
                    onJoystickMoved(drivingSensitivity,0, 0, joystick.getId());
                    return true;
                }
                return false;
            });
    }


    private void setInit() {
        joystickInitX = joystick.getX();
        joystickInitY = joystick.getY();
        joystickRadius = joystick.getHeight()/2f;
    }

    public void onJoystickMoved(float sensitivity, float xPercent, float yPercent, int id) {
        int angle = (int) ((xPercent * 100) * (sensitivity));
        int speed = (int) (yPercent * -100);

        if (car.getStatus() == ACTIVE) {
            car.setSteeringAngle(angle);
            car.setSpeed(speed);
        }
    }

    private void onBackbtnClicked(View view) {
        final Intent intent = new Intent(this, UserMenuActivity.class);
        startActivity(intent);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            hideSystemUI();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != RECORD_AUDIO_REQUEST_CODE || grantResults.length <= 0) {
            return;
        }

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show();
    }

    private void requestRequiredPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
    }

    private void onCameraFrameReceived(int[] pixels, int width, int height) {
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        cameraView.setImageBitmap(bitmap);
    }

    private void onSpeechResults(Bundle bundle) {
        micButton.setImageResource(R.drawable.ic_mic);

        final List<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (data == null || data.isEmpty()) {
            return;
        }

        final String command = data.get(0);
        System.out.println(data.get(0));
        final String[] commandParts = command.trim().split(" ");

        if (commandParts.length == 1) {
            voiceControl.executeCommand(commandParts[0]);
            return;
        }

        voiceControl.executeCommand(commandParts[0], Arrays.copyOfRange(commandParts, 1, commandParts.length));
    }

    private void fetchSensitivityFromDatabase() {
        databaseReference.child("users/" + firebaseUser.getUid() + "/sensitivity")
                .get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("ERROR", "Error getting data");
            } else {
                DataSnapshot getResult = task.getResult();
                if (getResult == null) {
                    return;
                }
                Number fetchedSens = (Number) task.getResult().getValue();
                if (fetchedSens == null) {
                    return;
                }
                drivingSensitivity = fetchedSens.floatValue();
            }
        });
    }

    private void fetchSensitivityLocally() {
        if (firebaseUser == null) {
            SharedPreferences sharedPreferences = getSharedPreferences("Preferences", MODE_PRIVATE);
            drivingSensitivity = sharedPreferences.getFloat("sensitivity", 1);
        }
    }
}
