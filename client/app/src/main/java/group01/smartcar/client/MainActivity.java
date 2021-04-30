package group01.smartcar.client;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import static group01.smartcar.client.Status.*;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener{
    CarControl car;
    protected ImageView cameraView;
    protected Vibrator vibrator;
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getResources();
        setContentView(R.layout.activity_main);
        registerComponentCallbacks();
        cameraView = findViewById(R.id.imageView);
        vibrator = (Vibrator) getSystemService(this.getApplicationContext().VIBRATOR_SERVICE);

        int localUrlId = res.getIdentifier("local_server_url", "string", this.getApplicationContext().getPackageName());
        if (localUrlId != 0) {
            car = new CarControl(this.getApplicationContext(), getString(localUrlId), cameraView);
        } else {
            car = new CarControl(this.getApplicationContext(), cameraView);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        car.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("OnResume");
        // Reconnect to MQTT server if application is resumed
        car.resume();
    }

    private void registerComponentCallbacks() {
        Switch sw = (Switch) findViewById(R.id.drive_park_switch);
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final VibrationEffect vibrationEffect1;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrationEffect1 = VibrationEffect.createOneShot(100, VibrationEffect.EFFECT_HEAVY_CLICK);
                vibrator.cancel();
                vibrator.vibrate(vibrationEffect1);
            }
            else {
                vibrator.vibrate(100);
            }
            if(isChecked) {
                car.start();
            } else {
                car.stop();
            }
        });
    }

    @Override
    public void onJoystickMoved(float xPercent, float yPercent, int id){
        int angle = (int)((xPercent) * 100);
        int speed = (int)((yPercent) * -100);
        Log.d("joystick", "angle: " + angle + " speed: " + speed );
        if(car.getStatus() == ACTIVE) {
            car.setSteeringAngle(angle);
            car.throttle(speed);
        }

    }

}