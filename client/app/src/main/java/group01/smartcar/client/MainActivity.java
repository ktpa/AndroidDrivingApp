package group01.smartcar.client;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import static group01.smartcar.client.Status.*;

public class MainActivity extends AppCompatActivity {
    SeekBar simpleSeekBar;
    CarControl car;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerComponentCallbacks();
        car = new CarControl(this.getApplicationContext());
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
        findViewById(R.id.start_button).setOnClickListener(this::onStartClick);
        findViewById(R.id.stop_button).setOnClickListener(this::onStopClick);
        findViewById(R.id.leftDir_button).setOnClickListener(this::onLeftDirClick);
        findViewById(R.id.rightDir_button).setOnClickListener(this::onRightDirClick);
        simpleSeekBar = findViewById(R.id.speed_slider);
        simpleSeekBar.setOnSeekBarChangeListener(this.onSeekBarChange);
    }

    private void onStartClick(View view) {
        car.start();
    }

    private void onStopClick(View view) {
        car.stop();
    }

    private void onLeftDirClick(View view) {
        car.steer(Direction.LEFT);
    }

    private void onRightDirClick(View view) {
        car.steer(Direction.RIGHT);
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChange = new SeekBar.OnSeekBarChangeListener() {
        int progressChangedValue = 0;

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            progressChangedValue = progress - (seekBar.getMax() / 2);
            car.throttle(progressChangedValue);

        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
        }

        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}