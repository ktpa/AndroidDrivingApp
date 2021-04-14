package group01.smartcar.client;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerComponentCallbacks();
    }

    private void registerComponentCallbacks() {
        findViewById(R.id.start_button).setOnClickListener(this::onStartClick);
        findViewById(R.id.stop_button).setOnClickListener(this::onStopClick);
        findViewById(R.id.leftDir_button).setOnClickListener(this::onLeftDirClick);
        findViewById(R.id.rightDir_button).setOnClickListener(this::onRightDirClick);
    }

    private void onStartClick(View view) {
        // TODO
    }

    private void onStopClick(View view) {
        // TODO
    }

    private void onLeftDirClick(View view) {
        // TODO
    }

    private void onRightDirClick(View view) {
        // TODO
    }
}