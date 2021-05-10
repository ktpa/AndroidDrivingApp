package group01.smartcar.client;

import android.content.Context;

import group01.smartcar.client.internal.InternalSmartCar;

public interface SmartCar {

    void start();

    void stop();

    void resume();

    void pause();

    Status getStatus();

    void setSteeringAngle(int angle);

    void setSpeed(int speed);

    int getDirection();

    void onCameraFrameReceived(CameraFrameReceivedCallback cameraFrameReceivedCallback);

    void onSpeedUpdated(SpeedUpdatedCallback speedUpdatedCallback);

    void onMotorPowerUpdated(MotorPowerUpdatedCallback motorPowerUpdatedCallback);

    static SmartCar createCar(Context context) {
        return new InternalSmartCar(context);
    }

    enum Status {
        ACTIVE,
        INACTIVE,
        PAUSED
    }

    interface CameraFrameReceivedCallback {
        void onCameraFrameReceived(int[] pixels, int width, int height);
    }

    interface SpeedUpdatedCallback {
        void onSpeedUpdated(double speed);
    }

    interface MotorPowerUpdatedCallback {
        void onMotorPowerUpdated(double power);
    }
}
