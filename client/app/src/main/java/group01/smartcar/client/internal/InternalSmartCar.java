package group01.smartcar.client.internal;

import android.content.Context;
import android.graphics.Color;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private Status status = INACTIVE;

    private double currentSpeedMS = 0;

    private int voiceDrivingDirection = 1;

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
        mqtt.publish(SmartCarTopics.CONTROL_STEERING, String.valueOf(angle), 1, mqttPublishListener);
    }

    @Override
    public void setSpeed(int speed) {
        if (mqtt.isConnected() && status == ACTIVE) {
            mqtt.publish(SmartCarTopics.CONTROL_SPEED, String.valueOf(speed), 1, mqttPublishListener);
            if (motorPowerUpdatedCallback != null) {
                motorPowerUpdatedCallback.onMotorPowerUpdated(speed);
            }
        }
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

    public void voiceControl(String results) {
        List<String> dictionary = Arrays.asList("forward", "reverse", "stop", "speed", "turn", "left", "right", "lean", "zero", "one", "two", "three", "four", "five", "0", "1", "2", "3", "4", "5");
        String[] trimmedResults = results.toLowerCase().split(" ");
        List<String> cleanResults = new ArrayList<>();

        for (String word:trimmedResults) {
            if(word.equals("stop")) {
                setSteeringAngle(0);
                setSpeed(0);
                return;
            }
            if(dictionary.contains(word)) {
                cleanResults.add(word);
            }
        }

        if(cleanResults.size() <= 0) {
            return;
        }
        switch(cleanResults.get(0)) {
            case "forward":
                setSpeed(50);
                voiceDrivingDirection = 1;
                break;
            case "reverse":
                setSpeed(-50);
                voiceDrivingDirection = -1;
                break;
            case "speed":
                if(cleanResults.size() <= 1) {
                    return;
                }
                switch (cleanResults.get(1)){
                    case "0":
                    case "zero":
                        setSpeed(0);
                        break;
                    case "1":
                    case "one":
                        setSpeed(20*voiceDrivingDirection);
                        break;
                    case "2":
                    case "two":
                        setSpeed(40*voiceDrivingDirection);
                        break;
                    case "3":
                    case "three":
                        setSpeed(60*voiceDrivingDirection);
                        break;
                    case "4":
                    case "four":
                        setSpeed(80*voiceDrivingDirection);
                        break;
                    case "5":
                    case "five":
                        setSpeed(100*voiceDrivingDirection);
                        break;
                }
                break;
            case "turn":
            case "lean":
                // case lean will be differentiated in the future.
                if(cleanResults.size() <= 1) {
                    return;
                } else if (cleanResults.get(1).equals("left")) {
                    setSteeringAngle(-50);
                } else if (cleanResults.get(1).equals("right")) {
                    setSteeringAngle(50);
                }
                break;
        }
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
            }

            if(topic.equals(SmartCarTopics.TELEMETRY_SPEED)){
                final double newSpeedMS = Double.parseDouble(message.toString());
                if (currentSpeedMS == newSpeedMS) {
                    return;
                }

                currentSpeedMS = newSpeedMS;

                if (speedUpdatedCallback != null) {
                    speedUpdatedCallback.onSpeedUpdated(currentSpeedMS);
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
