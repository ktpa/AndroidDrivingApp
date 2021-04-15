package group01.smartcar.client;
import android.content.Context;
import group01.smartcar.client.Status;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import static group01.smartcar.client.Status.*;

public class CarControl {
    MqttClient mqtt;
    private final String DEFAULT_SERVER_URL = "tcp://hysm.dev:1883";
    private final String DEFAULT_CLIENT_ID = "CarApp";
    private final String SUBSCRIBE_URI = "/smartcar/control/#";
    private final String STEERING_URI = "/smartcar/control/steering";
    private final String THROTTLE_URI = "/smartcar/control/speed";

    private String username = "app_user";
    private String password = "app_pass";

    private Status status = INACTIVE;
    int steeringAngle = 0;
    int throttle = 0;

    public CarControl(Context context) {
        mqtt = new MqttClient(context, DEFAULT_SERVER_URL, DEFAULT_CLIENT_ID);
    }

    public CarControl(Context context, String serverUrl, String clientId) {
        mqtt = new MqttClient(context, serverUrl, clientId);
    }

    public CarControl(Context context, String serverUrl, String clientId, String username, String password) {
        this.username = username;
        this.password = password;
        mqtt = new MqttClient(context, serverUrl, clientId);
    }

    public void connect() {
        if (!mqtt.isConnected()) {
            try {
                mqtt.connect(username, password, mqttConnectionListener, mqttCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        if (mqtt.isConnected()) {
            try {
                mqtt.disconnect(mqttDisconnectListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            disconnect();
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

    public void steer(Direction direction) {
        if (mqtt.isConnected() && status == ACTIVE) {
            switch (direction) {
                case LEFT: steerLeft(-10); break;
                case RIGHT: steerRight(10); break;
            }
        }
    }

    private void steerLeft(int angle) {
        steeringAngle = (steeringAngle <= - 90) ? -90 : steeringAngle + angle;
        setSteeringAngle(steeringAngle);
    }

    private void steerRight(int angle) {
        steeringAngle = (steeringAngle >= 90) ? 90 : steeringAngle + angle;
        setSteeringAngle(steeringAngle);
    }

    private void setSteeringAngle(int angle) {
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
            if (topic.equals(STEERING_URI)) {
                steeringAngle = Integer.getInteger(message.toString());
            } else if (topic.equals(THROTTLE_URI)) {
                throttle = Integer.getInteger(message.toString());
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
            mqtt.subscribe(SUBSCRIBE_URI, 1, mqttSubscriptionListener);
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
            System.out.println("Published...");
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

}
