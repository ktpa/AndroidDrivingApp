package group01.smartcar.client.internal.speech.commands;

import java.util.HashSet;
import java.util.Set;

import group01.smartcar.client.SmartCar;
import group01.smartcar.client.internal.speech.VoiceControlCommand;

public class SpeedCommand implements VoiceControlCommand {

    private final Set<String> parameters = new HashSet<String>() {
        {
        add("0");
        add("1");
        add("2");
        add("3");
        add("4");
        add("5");
        add("zero");
        add("one");
        add("two");
        add("three");
        add("four");
        add("five");
        }
    };

    @Override
    public String getName() {
        return "speed";
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
            case "0":
            case "zero":
                smartCar.setSpeed(0);
                break;
            case "1":
            case "one":
                smartCar.setSpeed(20 * smartCar.getDirection());
                break;
            case "2":
            case "two":
                smartCar.setSpeed(40 * smartCar.getDirection());
                break;
            case "3":
            case "three":
                smartCar.setSpeed(60 * smartCar.getDirection());
                break;
            case "4":
            case "four":
                smartCar.setSpeed(80 * smartCar.getDirection());
                break;
            case "5":
            case "five":
                smartCar.setSpeed(100 * smartCar.getDirection());
                break;
            default:
                return false;
        }

        return true;
    }
}
