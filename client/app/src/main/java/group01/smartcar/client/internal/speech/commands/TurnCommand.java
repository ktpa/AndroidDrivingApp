package group01.smartcar.client.internal.speech.commands;

import java.util.Collections;
import java.util.List;

import group01.smartcar.client.SmartCar;
import group01.smartcar.client.internal.speech.VoiceControlCommand;

public class TurnCommand implements VoiceControlCommand {

    @Override
    public String getName() {
        return "turn";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("lean");
    }

    @Override
    public void execute(SmartCar smartCar, String... parameters) {
        if (parameters != null && parameters.length < 1) {
            return;
        }

        switch (parameters[0]) {
            case "left":
                smartCar.setSteeringAngle(-50);
                break;
            case "right":
                smartCar.setSteeringAngle(50);
                break;
        }
    }
}
