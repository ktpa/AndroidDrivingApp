package group01.smartcar.client.internal.speech.commands;

import group01.smartcar.client.SmartCar;
import group01.smartcar.client.internal.speech.VoiceControlCommand;

public class SpeedCommand implements VoiceControlCommand {

    @Override
    public String getName() {
        return "speed";
    }

    @Override
    public void execute(SmartCar smartCar, String... parameters) {
        if (parameters != null && parameters.length < 1) {
            return;
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
        }
    }
}
