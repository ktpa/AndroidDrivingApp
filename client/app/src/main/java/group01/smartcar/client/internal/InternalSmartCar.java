package group01.smartcar.client.internal;

import android.content.Context;
import android.graphics.Color;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import group01.smartcar.client.SmartCar;
import group01.smartcar.client.internal.mqtt.SmartCarTopics;
import group01.smartcar.client.internal.resources.Secrets;
import group01.smartcar.client.mqtt.MqttClient;

import static group01.smartcar.client.SmartCar.Status.ACTIVE;
import static group01.smartcar.client.SmartCar.Status.INACTIVE;
import static group01.smartcar.client.SmartCar.Status.PAUSED;

public class InternalSmartCar implements SmartCar {

    private static final int IMAGE_WIDTH = 320;
    private static final int IMAGE_HEIGHT = 240;

    private final MqttClient mqtt;

    private CameraFrameReceivedCallback cameraFrameReceivedCallback;
    private SpeedUpdatedCallback speedUpdatedCallback;
    private MotorPowerUpdatedCallback motorPowerUpdatedCallback;
    private ProximitySensorUpdatedCallback frontSensorUpdatedCallback;
    private ProximitySensorUpdatedCallback backSensorUpdatedCallback;

    private Status status = INACTIVE;

    private double currentSpeedMS = 0;

    private int direction = 1;

    public InternalSmartCar(Context context) {
        mqtt = new MqttClient(context, Secrets.Mqtt.getServerUrl(), Secrets.Mqtt.getClientId());
    }

    @Override
    public void start() {
        connect();
    }

    @Override
    public void stop() {
        if (status == ACTIVE) {
            setSpeed(0);
            setSteeringAngle(0);
            this.disconnect();
            status = INACTIVE;
        }
    }

    @Override
    public void pause() {
        if (status == ACTIVE) {
            status = PAUSED;
            this.disconnect();
        }
    }

    @Override
    public void resume() {
        System.out.println("resume(): " + status.name());
        if (status == PAUSED) {
            status = ACTIVE;
            this.connect();
        }
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setSteeringAngle(int angle) {
        /* Caps the value of the angle of the car's movement to 90 degrees or -90 degrees to save on
           data transmission */
        angle = angle < -90 ? -90 : Math.min(angle, 90);
        mqtt.publish(SmartCarTopics.CONTROL_STEERING, String.valueOf(angle), 1, mqttPublishListener);
    }

    @Override
    public void setSpeed(int speed) {
        if (mqtt.isConnected() && status == ACTIVE) {
            mqtt.publish(SmartCarTopics.CONTROL_SPEED, String.valueOf(speed), 1, mqttPublishListener);
            if (motorPowerUpdatedCallback != null) {
                motorPowerUpdatedCallback.onMotorPowerUpdated(speed);
            }

            direction = Integer.signum(speed);
        }
    }

    @Override
    public int getDirection() {
        return direction;
    }

    @Override
    public void onCameraFrameReceived(CameraFrameReceivedCallback cameraFrameReceivedCallback) {
        this.cameraFrameReceivedCallback = cameraFrameReceivedCallback;
    }

    @Override
    public void onSpeedUpdated(SpeedUpdatedCallback speedUpdatedCallback) {
        this.speedUpdatedCallback = speedUpdatedCallback;
    }

    @Override
    public void onMotorPowerUpdated(MotorPowerUpdatedCallback motorPowerUpdatedCallback) {
        this.motorPowerUpdatedCallback = motorPowerUpdatedCallback;
    }

    @Override
    public void onFrontSensorUpdated(ProximitySensorUpdatedCallback proximitySensorUpdatedCallback) {
        this.frontSensorUpdatedCallback = proximitySensorUpdatedCallback;
    }

    @Override
    public void onBackSensorUpdated(ProximitySensorUpdatedCallback proximitySensorUpdatedCallback) {
        this.backSensorUpdatedCallback = proximitySensorUpdatedCallback;
    }

    private void connect() {
        try {
            if (!mqtt.isConnected()) {
                mqtt.connect(Secrets.Mqtt.getUsername(), Secrets.Mqtt.getPassword(), mqttConnectionListener, mqttCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            if (mqtt.isConnected()) {
                mqtt.disconnect(mqttDisconnectListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {

        }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        if (topic.equals(SmartCarTopics.CAMERA)) {
            final byte[] payload = message.getPayload();
            final int[] pixels = new int[IMAGE_WIDTH * IMAGE_HEIGHT];
            for (int ci = 0; ci < pixels.length; ++ci) {
                final byte r = payload[3 * ci];
                final byte g = payload[3 * ci + 1];
                final byte b = payload[3 * ci + 2];
                pixels[ci] = Color.rgb(r, g, b);
            }

            if (cameraFrameReceivedCallback != null) {
                cameraFrameReceivedCallback.onCameraFrameReceived(pixels, IMAGE_WIDTH, IMAGE_HEIGHT);
            }

            return;
        }

        if(topic.equals(SmartCarTopics.TELEMETRY_SPEED)) {
            final double newSpeedMS = Double.parseDouble(message.toString());
            if (currentSpeedMS == newSpeedMS) {
                return;
            }

            currentSpeedMS = newSpeedMS;

            if (speedUpdatedCallback != null) {
                speedUpdatedCallback.onSpeedUpdated(currentSpeedMS);
            }

            return;
        }

        if (topic.equals(SmartCarTopics.TELEMETRY_FRONT_ULTRASONIC)) {
            final int distance = Integer.parseInt(message.toString());

            if (backSensorUpdatedCallback != null) {
                backSensorUpdatedCallback.onProximitySensorUpdated(distance);
            }

            return;
        }

        if (topic.equals(SmartCarTopics.TELEMETRY_BACK_INFRARED)) {
            final int distance = Integer.parseInt(message.toString());

            if (backSensorUpdatedCallback != null) {
                backSensorUpdatedCallback.onProximitySensorUpdated(distance);
            }
        }
    }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

    private final IMqttActionListener mqttConnectionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            mqtt.subscribe(SmartCarTopics.CAMERA, 1, mqttSubscriptionListener);
            mqtt.subscribe(SmartCarTopics.TELEMETRY_SPEED, 1, mqttSubscriptionListener);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            status = INACTIVE;
            System.out.println("Failed to connect: " + exception.getLocalizedMessage());
        }
    };

    private final IMqttActionListener mqttSubscriptionListener = new IMqttActionListener() {

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

    private final IMqttActionListener mqttPublishListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            System.out.println("Failed to publish.");
        }
    };

    private final IMqttActionListener mqttDisconnectListener = new IMqttActionListener() {
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
