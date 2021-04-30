package group01.smartcar.client;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import static group01.smartcar.client.Status.*;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener{
    CarControl car;
    protected ImageView cameraView;
    protected TextView speedometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerComponentCallbacks();
        cameraView = findViewById(R.id.imageView);
        speedometer = findViewById(R.id.simpleSpeedometer);
        car = new CarControl(this.getApplicationContext(), cameraView, speedometer);
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