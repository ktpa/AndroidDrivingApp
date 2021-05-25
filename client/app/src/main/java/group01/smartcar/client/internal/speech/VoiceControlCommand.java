package group01.smartcar.client.internal.speech;

import java.util.Collections;
import java.util.Set;

import group01.smartcar.client.SmartCar;

public interface VoiceControlCommand {
    
    String getName();

    default Set<String> getAliases() {
        return Collections.emptySet();
    }

    default boolean hasParameters() {
        return false;
    }

    default boolean hasParameter(String parameter) {
        return false;
    }

    boolean execute(SmartCar smartCar, String... parameters);
}
