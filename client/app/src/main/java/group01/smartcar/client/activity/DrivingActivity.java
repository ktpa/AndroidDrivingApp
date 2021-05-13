package group01.smartcar.client.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.SpeechRecognizer;
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
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import group01.smartcar.client.R;
import group01.smartcar.client.SmartCar;
import group01.smartcar.client.SmartCarApplication;
import group01.smartcar.client.SmartCarVoiceControl;
import group01.smartcar.client.speech.SpeechListener;
import group01.smartcar.client.view.Speedometer;

import static group01.smartcar.client.SmartCar.Status.ACTIVE;


// 78 to 112 adapted from https://developer.android.com/training/system-ui/immersive .

public class DrivingActivity extends AppCompatActivity {
    private static final Integer RECORD_AUDIO_REQUEST_CODE = 1;
    private static final int DEFAULT_SENSITIVITY = 1;

    private View joystick;
    private SpringAnimation animJoystickY;
    private SpringAnimation animJoystickX;
    private final float joystickRadiusMax = 180;
    private float joystickInitX;
    private float joystickInitY;
    private boolean notSetInit;
    private float joystickRadius;

    private SmartCar car;
    private ImageView cameraView;
    private Speedometer speedometer;
    private Vibrator vibrator;
    private ImageView micButton;
    private SpeechListener speechListener;

    private SmartCarVoiceControl voiceControl;

    private ScheduledFuture<?> speedometerUpdater;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        requestRequiredPermissions();

        cameraView = findViewById(R.id.imageView);
        speedometer = findViewById(R.id.fancySpeedometer);
        micButton = findViewById(R.id.micButton);
        joystick = findViewById(R.id.joystick);

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        car.resume();

        if (speedometerUpdater != null && speedometerUpdater.isCancelled()) {
            speedometerUpdater = SmartCarApplication.getTaskExecutor().scheduleTask(speedometer::update);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        car.pause();
        speedometerUpdater.cancel(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        speechListener.destroy();
        speedometerUpdater.cancel(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void registerComponentCallbacks() {
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        final Switch sw = findViewById(R.id.drive_park_switch);


        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final VibrationEffect vibrationEffect1;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrationEffect1 = VibrationEffect.createOneShot(100, VibrationEffect.EFFECT_HEAVY_CLICK);
                vibrator.cancel();
                vibrator.vibrate(vibrationEffect1);
            } else {
                vibrator.vibrate(100);
            }

            if(isChecked) {
                car.start();
            } else {
                car.stop();
            }
        });

        micButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                micButton.setImageResource(R.drawable.ic_mic_black_off);
                speechListener.stop();
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                micButton.setImageResource(R.drawable.ic_mic_black_24dp);
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
                    joystick.setX(motionEvent.getRawX() - (joystick.getWidth() / 2f));
                    joystick.setY(motionEvent.getRawY() - (joystick.getHeight() / 2f));
                } else {
                    joystick.setX((((motionEvent.getRawX() - (joystick.getWidth() / 2f)) - joystick.getLeft()) * joystickRadiusMax/touchDistance) + joystick.getLeft());
                    joystick.setY((((motionEvent.getRawY() - (joystick.getHeight() / 2f)) - joystick.getTop()) * joystickRadiusMax/touchDistance) + joystick.getTop());
                }
                    onJoystickMoved(DEFAULT_SENSITIVITY,(joystick.getX() - joystickInitX) / joystickRadius, (joystick.getY() - joystickInitY) / joystickRadius, joystick.getId());
                    return true;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    animJoystickX.start();
                    animJoystickY.start();
                    onJoystickMoved(DEFAULT_SENSITIVITY,0, 0, joystick.getId());
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

    public void onJoystickMoved(double sensitivity, float xPercent, float yPercent, int id) {
        int angle = (int) ((int) (xPercent * 100) * (sensitivity));
        int speed = (int) (yPercent * -100);

        if (car.getStatus() == ACTIVE) {
            car.setSteeringAngle(angle);
            car.setSpeed(speed);
        }
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
        micButton.setImageResource(R.drawable.ic_mic_black_off);

        final List<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (data == null || data.isEmpty()) {
            return;
        }

        final String command = data.get(0);

        final String[] commandParts = command.trim().split(" ");

        if (commandParts.length == 1) {
            voiceControl.executeCommand(commandParts[0]);
            return;
        }

        voiceControl.executeCommand(commandParts[0], Arrays.copyOfRange(commandParts, 1, commandParts.length));
    }

}
