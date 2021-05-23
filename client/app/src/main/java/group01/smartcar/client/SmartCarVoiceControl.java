package group01.smartcar.client;

import group01.smartcar.client.internal.InternalSmartCarVoiceControl;

public interface SmartCarVoiceControl {

    boolean executeCommand(String commandName, String... parameters);

    boolean commandExists(String commandName);

    static SmartCarVoiceControl create(SmartCar smartCar) {
        return new InternalSmartCarVoiceControl(smartCar);
    }
}
