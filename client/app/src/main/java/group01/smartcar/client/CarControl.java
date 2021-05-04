package group01.smartcar.client;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import static group01.smartcar.client.Status.*;

public class CarControl {
    MqttClient mqtt;
    private final String DEFAULT_SERVER_URL = "tcp://10.0.2.2:1883";
    private final String DEFAULT_CLIENT_ID = "CarApp";
    private final String STEERING_URI = "/smartcar/control/steering";
    private final String THROTTLE_URI = "/smartcar/control/speed";
    private final String SPEED_URI = "/smartcar/control/speedMS";
    private final String CAMERA_URI = "/smartcar/camera";

    private static final int IMAGE_WIDTH = 320;
    private static final int IMAGE_HEIGHT = 240;
    private ImageView cameraView;
    private Speedometer speedometer;

    private String username = "app_user";
    private String password = "app_pass";

    private Status status = INACTIVE;
    int steeringAngle = 0;
    double currentSpeedMS=0;

    public CarControl(Context context, ImageView cameraView, Speedometer speedometer) {
        mqtt = new MqttClient(context, DEFAULT_SERVER_URL, DEFAULT_CLIENT_ID);
        this.cameraView = cameraView;
        this.speedometer = speedometer;
    }

    public CarControl(Context context, String serverUrl, String clientId, ImageView cameraView, Speedometer speedometer) {
        mqtt = new MqttClient(context, serverUrl, clientId);
        this.cameraView = cameraView;
        this.speedometer = speedometer;
    }

    public CarControl(Context context, String serverUrl, String clientId, String username, String password, ImageView cameraView, Speedometer speedometer) {
        this.username = username;
        this.password = password;
        mqtt = new MqttClient(context, serverUrl, clientId);
        this.cameraView = cameraView;
        this.speedometer = speedometer;
    }

    public void connect() {
        try {
            if (!mqtt.isConnected()) {

                mqtt.connect(username, password, mqttConnectionListener, mqttCallback);
            }
        } catch (Exception e) {
                e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (mqtt.isConnected()) {
                mqtt.disconnect(mqttDisconnectListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return mqtt.isConnected();
    }

    public Status getStatus() {
        return status;
    }

    public void start() {
        connect();
    }

    public void stop() {
        if (status == ACTIVE) {
            throttle(0);
            setSteeringAngle(0);
            this.disconnect();
            status = INACTIVE;
        }
    }

    public void pause() {
        if (status == ACTIVE) {
            System.out.println("Pausing...");
            status = PAUSED;
            this.disconnect();
        }
    }

    public void resume() {
        System.out.println("resume(): " + status.name());
        if (status == PAUSED) {
            System.out.println("Resuming...");
            status = ACTIVE;
            this.connect();
        }
    }
    public void setSteeringAngle(int angle) {
        steeringAngle = angle;
        mqtt.publish(STEERING_URI, String.valueOf(steeringAngle), 1, mqttPublishListener);
    }

    public void throttle(int speed) {
        if (mqtt.isConnected() && status == ACTIVE) {
            mqtt.publish(THROTTLE_URI, String.valueOf(speed), 1, mqttPublishListener);
        }
    }


    MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            if (topic.equals(CAMERA_URI)) {
                final Bitmap bm = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
                final byte[] payload = message.getPayload();
                final int[] colors = new int[IMAGE_WIDTH * IMAGE_HEIGHT];
                for (int ci = 0; ci < colors.length; ++ci) {
                    final byte r = payload[3 * ci];
                    final byte g = payload[3 * ci + 1];
                    final byte b = payload[3 * ci + 2];
                    colors[ci] = Color.rgb(r, g, b);
                }
                bm.setPixels(colors, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

                cameraView.setImageBitmap(bm);
            }
            if(topic.equals(SPEED_URI)){
                double newSpeedMS = Double.parseDouble(message.toString());
                if(currentSpeedMS != newSpeedMS){
                    currentSpeedMS=newSpeedMS;
                    speedometer.setCurrentSpeedMS(currentSpeedMS);
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

    IMqttActionListener mqttConnectionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            // Try to subscribe to the main car resource
            mqtt.subscribe(CAMERA_URI, 1, mqttSubscriptionListener);
            mqtt.subscribe(SPEED_URI, 1, mqttSubscriptionListener);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            // Something went wrong
            status = INACTIVE;
            System.out.println("Failed to connect: " + exception.getLocalizedMessage());
        }
    };

    IMqttActionListener mqttSubscriptionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            status = ACTIVE;
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            status = INACTIVE;
            disconnect();
        }
    };

    IMqttActionListener mqttPublishListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            //System.out.println("Published...");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            System.out.println("Failed to publish.");
        }
    };

    IMqttActionListener mqttDisconnectListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            if (status == ACTIVE) {
                status = INACTIVE;
            }
            System.out.println("Disconnected!");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            status = INACTIVE;
            System.out.println("Failed to disconnect. Reason: " + exception.getLocalizedMessage());
        }
    };

    private String getCurrentSpeedKMHString(){
        double currentSpeedKMH = currentSpeedMS * 3.6;
        String twoDigit = Double.toString(currentSpeedKMH);
        return twoDigit.substring(0, Math.min(twoDigit.length(), 3)) + " km/h";
    }



}
