package group01.smartcar.client.internal.speech;

import java.util.Collections;
import java.util.List;

import group01.smartcar.client.SmartCar;

public interface VoiceControlCommand {
    
    String getName();

    default List<String> getAliases() {
        return Collections.emptyList();
    }

    void execute(SmartCar smartCar, String... parameters);
}
