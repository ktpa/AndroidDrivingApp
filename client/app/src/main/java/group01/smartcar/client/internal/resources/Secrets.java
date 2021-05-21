package group01.smartcar.client.internal.resources;

import group01.smartcar.client.BuildConfig;

public final class Secrets {

    private Secrets() {
    }

    public static final class Mqtt {

        private Mqtt() {
        }

        public static String getServerUrl() {
            return BuildConfig.MQTT_SERVER_URL;
        }

        public static String getClientId() {
            return BuildConfig.MQTT_CLIENT_ID;
        }

        public static String getUsername() {
            return BuildConfig.MQTT_USERNAME;
        }

        public static String getPassword() {
            return BuildConfig.MQTT_PASSWORD;
        }
    }

    public static final class LoginTestCredentials {

        private  LoginTestCredentials() {
        }

        public static String getLoginTestUsername() { return BuildConfig.LOGIN_TEST_USERNAME; }

        public static String getLoginTestPassword() { return BuildConfig.LOGIN_TEST_PASSWORD; }

    }
}
