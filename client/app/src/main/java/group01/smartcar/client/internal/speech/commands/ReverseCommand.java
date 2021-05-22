package group01.smartcar.client.internal.speech.commands;

import group01.smartcar.client.SmartCar;
import group01.smartcar.client.internal.speech.VoiceControlCommand;

public class ReverseCommand implements VoiceControlCommand {

    @Override
    public String getName() {
        return "reverse";
    }

    @Override
    public boolean execute(SmartCar smartCar, String... parameters) {
        smartCar.setSpeed(-50);

        return true;
    }
}
