package group01.smartcar.client.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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

import group01.smartcar.client.R;
import group01.smartcar.client.SmartCar;
import group01.smartcar.client.speech.SpeechControl;
import group01.smartcar.client.view.Joystick;
import group01.smartcar.client.view.Speedometer;

import static group01.smartcar.client.SmartCar.Status.ACTIVE;

// 78 to 112 adapted from https://developer.android.com/training/system-ui/immersive .

public class DrivingActivity extends AppCompatActivity implements Joystick.JoystickListener {
    private SmartCar car;
    private Speedometer speedometer;
    private Vibrator vibrator;
    private ImageView micButton;
    private SpeechControl speechControl;
    public static final Integer RecordAudioRequestCode = 1;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drive);

        requestRequiredPermissions();

        final ImageView cameraView = findViewById(R.id.imageView);
        speedometer = findViewById(R.id.fancySpeedometer);
        car = new SmartCar(this.getApplicationContext(), cameraView, speedometer);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        micButton = findViewById(R.id.micButton);

        speechControl = new SpeechControl(this);
        speechControl.onResults(bundle -> {
            micButton.setImageResource(R.drawable.ic_mic_black_off);
            List<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            System.out.println(data.get(0));
            car.voiceControl(data.get(0));
        });

        registerComponentCallbacks();

        AsyncTask.execute(() -> {
            while (true) {
                speedometer.update();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        car.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        car.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        speechControl.destroy();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void registerComponentCallbacks() {
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
                speechControl.stop();

                return true;
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                micButton.setImageResource(R.drawable.ic_mic_black_24dp);
                speechControl.start();

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
            car.throttle(speed);
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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

        if (requestCode != RecordAudioRequestCode || grantResults.length <= 0) {
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
    }

}
