package group01.smartcar.client.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import group01.smartcar.client.SmartCar;
import group01.smartcar.client.SmartCarVoiceControl;
import group01.smartcar.client.internal.speech.VoiceControlCommand;
import group01.smartcar.client.internal.speech.commands.ForwardCommand;
import group01.smartcar.client.internal.speech.commands.ReverseCommand;
import group01.smartcar.client.internal.speech.commands.SpeedCommand;
import group01.smartcar.client.internal.speech.commands.StopCommand;
import group01.smartcar.client.internal.speech.commands.TurnCommand;

public class InternalSmartCarVoiceControl implements SmartCarVoiceControl {

    private final List<VoiceControlCommand> commands = new ArrayList<>();

    private final SmartCar smartCar;

    public InternalSmartCarVoiceControl(SmartCar smartCar) {
        this.smartCar = smartCar;

        injectCommands();
    }

    @Override
    public void executeCommand(String commandName, String... parameters) {
        final Optional<VoiceControlCommand> command = getCommand(commandName);

        if (!command.isPresent()) {
            return;
        }

        command.get().execute(smartCar, parameters);
    }

    private Optional<VoiceControlCommand> getCommand(String identifier) {
        for (VoiceControlCommand command : commands) {
            if (command.getName().equalsIgnoreCase(identifier) || command.getAliases().contains(identifier)) {
                return Optional.of(command);
            }
        }

        return Optional.empty();
    }

    private void injectCommands() {
        commands.add(new ForwardCommand());
        commands.add(new ReverseCommand());
        commands.add(new SpeedCommand());
        commands.add(new StopCommand());
        commands.add(new TurnCommand());
    }
}
