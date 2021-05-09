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

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import group01.smartcar.client.R;
import group01.smartcar.client.SmartCar;
import group01.smartcar.client.async.TaskExecutor;
import group01.smartcar.client.speech.SpeechListener;
import group01.smartcar.client.view.Joystick;
import group01.smartcar.client.view.Speedometer;

import static group01.smartcar.client.SmartCar.Status.ACTIVE;


// 78 to 112 adapted from https://developer.android.com/training/system-ui/immersive .

public class DrivingActivity extends AppCompatActivity implements Joystick.JoystickListener {
    private static final Integer RECORD_AUDIO_REQUEST_CODE = 1;

    private SmartCar car;
    private ImageView cameraView;
    private Speedometer speedometer;
    private Vibrator vibrator;
    private ImageView micButton;
    private SpeechListener speechListener;

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

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        car = SmartCar.createCar(getApplicationContext());
        car.onCameraFrameReceived(this::onCameraFrameReceived);
        car.onSpeedUpdated(speedometer::setCurrentSpeedMS);
        car.onMotorPowerUpdated(speedometer::setMotorPowerPercentage);

        speechListener = new SpeechListener(this);
        speechListener.onResults(bundle -> {
            micButton.setImageResource(R.drawable.ic_mic_black_off);
            List<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            System.out.println(data.get(0));
//            car.voiceControl(data.get(0)); TODO: decouple car voice control from SmartCar class
        });

        registerComponentCallbacks();

        speedometerUpdater = TaskExecutor.getInstance().scheduleTask(speedometer::update);
    }

    @Override
    protected void onResume() {
        super.onResume();

        car.resume();

        if (speedometerUpdater != null && speedometerUpdater.isCancelled()) {
            speedometerUpdater = TaskExecutor.getInstance().scheduleTask(speedometer::update);
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
                view.performClick();
                speechListener.stop();

                return true;
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                micButton.setImageResource(R.drawable.ic_mic_black_24dp);
                speechListener.start();

                return true;
            }

            return false;
        });
    }

    @Override
    public void onJoystickMoved(float xPercent, float yPercent, int id){
        int angle = (int) (xPercent * 100);
        int speed = (int) (yPercent * -100);

        if (car.getStatus() == ACTIVE) {
            car.setSteeringAngle(angle);
            car.setSpeed(speed);
        }
    }


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

}
