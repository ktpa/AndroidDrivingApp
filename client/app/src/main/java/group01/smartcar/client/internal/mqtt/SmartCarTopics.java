package group01.smartcar.client.internal.mqtt;

public final class SmartCarTopics {
    public static final String CONTROL_STEERING = "/smartcar/control/steering";
    public static final String CONTROL_SPEED = "/smartcar/control/speed";
    public static final String TELEMETRY_SPEED = "/smartcar/telemetry/speed";
    public static final String TELEMETRY_FRONT_ULTRASONIC = "/smartcar/telemetry/frontUltrasonic";
    public static final String TELEMETRY_BACK_INFRARED = "/smartcar/telemetry/backInfrared";
    public static final String CAMERA = "/smartcar/camera";

    private SmartCarTopics() {
    }
}
