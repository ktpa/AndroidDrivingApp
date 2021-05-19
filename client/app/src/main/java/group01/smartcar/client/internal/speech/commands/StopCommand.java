package group01.smartcar.client.internal.speech.commands;

import group01.smartcar.client.SmartCar;
import group01.smartcar.client.internal.speech.VoiceControlCommand;

public class StopCommand implements VoiceControlCommand {

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public void execute(SmartCar smartCar, String... parameters) {
        smartCar.setSpeed(0);
        smartCar.setSteeringAngle(0);
    }
}
