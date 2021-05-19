package group01.smartcar.client.mqtt;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static java.util.Objects.requireNonNull;

// Adapted from: https://medium.com/swlh/android-and-mqtt-a-simple-guide-cb0cbba1931c

public class MqttClient {

    private final MqttAndroidClient client;

    public MqttClient(Context context, String serverUrl, String clientId) {
        client = new MqttAndroidClient(context, serverUrl, clientId);
    }

    public void connect(String username, String password, IMqttActionListener connectionCallback, MqttCallback clientCallback) {
        client.setCallback(requireNonNull(clientCallback));
        
        final MqttConnectOptions options = new MqttConnectOptions();
        options.setMaxInflight(1000);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        try {
            client.connect(options, null, connectionCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(IMqttActionListener disconnectionCallback) {
        try {
            client.disconnect(null, disconnectionCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic, int qos, IMqttActionListener subscriptionCallback) {
        try {
            client.subscribe(topic, qos, null, subscriptionCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe(String topic, IMqttActionListener unsubscriptionCallback) {
        try {
            client.unsubscribe(topic, null, unsubscriptionCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String message, int qos, IMqttActionListener publishCallback) {
        final MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes());
        mqttMessage.setQos(qos);

        try {
            client.publish(topic, mqttMessage, null, publishCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return client.isConnected();
    }
}
