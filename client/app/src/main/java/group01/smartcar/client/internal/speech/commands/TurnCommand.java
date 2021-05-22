package group01.smartcar.client.internal.speech.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import group01.smartcar.client.SmartCar;
import group01.smartcar.client.internal.speech.VoiceControlCommand;

public class TurnCommand implements VoiceControlCommand {

    private final Set<String> parameters = new HashSet<String>() {
        {
        add("left");
        add("right");
        }
    };

    private final Set<String> aliases = Collections.singleton("lean");

    @Override
    public String getName() {
        return "turn";
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public boolean hasParameter(String parameter) {
        return parameters.contains(parameter);
    }

    @Override
    public boolean execute(SmartCar smartCar, String... parameters) {
        if (parameters == null || parameters.length < 1) {
            return false;
        }

        switch (parameters[0]) {
            case "left":
                smartCar.setSteeringAngle(-50);
                break;
            case "right":
                smartCar.setSteeringAngle(50);
                break;
            default:
                return false;
        }

        return true;
    }
}
