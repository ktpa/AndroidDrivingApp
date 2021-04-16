package group01.smartcar.client;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

// Adapted from: https://medium.com/swlh/android-and-mqtt-a-simple-guide-cb0cbba1931c

public class MqttClient {

    private MqttAndroidClient mMqttAndroidClient;

    public MqttClient(Context context, String serverUrl, String clientId) {
        mMqttAndroidClient = new MqttAndroidClient(context, serverUrl, clientId);
    }

    public void connect(String username, String password, IMqttActionListener connectionCallback, MqttCallback clientCallback) {
        mMqttAndroidClient.setCallback(clientCallback);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        try {
            mMqttAndroidClient.connect(options, null, connectionCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(IMqttActionListener disconnectionCallback) {
        try {
            mMqttAndroidClient.disconnect(null, disconnectionCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic, int qos, IMqttActionListener subscriptionCallback) {
        try {
            mMqttAndroidClient.subscribe(topic, qos, null, subscriptionCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe(String topic, IMqttActionListener unsubscriptionCallback) {
        try {
            mMqttAndroidClient.unsubscribe(topic, null, unsubscriptionCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String message, int qos, IMqttActionListener publishCallback) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes());
        mqttMessage.setQos(qos);

        try {
            mMqttAndroidClient.publish(topic, mqttMessage, null, publishCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return mMqttAndroidClient.isConnected();
    }
}
