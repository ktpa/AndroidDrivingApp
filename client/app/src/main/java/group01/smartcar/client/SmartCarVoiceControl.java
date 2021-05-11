package group01.smartcar.client;

import group01.smartcar.client.internal.InternalSmartCarVoiceControl;

public interface SmartCarVoiceControl {

    void executeCommand(String commandName, String... parameters);

    static SmartCarVoiceControl create(SmartCar smartCar) {
        return new InternalSmartCarVoiceControl(smartCar);
    }
}
