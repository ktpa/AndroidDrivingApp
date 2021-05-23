package group01.smartcar.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;

@RunWith(MockitoJUnitRunner.class)
public final class SmartCarVoiceControlTest {

    @Mock
    private SmartCar smartCar;

    @Test
    public void executeCommand_forward() {
        doNothing().when(smartCar).setSpeed(anyInt());
        doNothing().when(smartCar).setSteeringAngle(anyInt());

        final SmartCarVoiceControl voiceControl = SmartCarVoiceControl.create(smartCar);

        assertTrue(voiceControl.executeCommand("forward"));
        assertTrue(voiceControl.executeCommand("forward", "unknown", "arguments"));
    }

    @Test
    public void executeCommand_reverse() {
        doNothing().when(smartCar).setSpeed(anyInt());

        final SmartCarVoiceControl voiceControl = SmartCarVoiceControl.create(smartCar);

        assertTrue(voiceControl.executeCommand("reverse"));
        assertTrue(voiceControl.executeCommand("reverse", "unknown", "arguments"));
    }

    @Test
    public void executeCommand_speed() {
        doNothing().when(smartCar).setSpeed(anyInt());

        final SmartCarVoiceControl voiceControl = SmartCarVoiceControl.create(smartCar);

        assertFalse(voiceControl.executeCommand("speed"));
        assertFalse(voiceControl.executeCommand("speed", "unknown", "arguments"));

        assertTrue(voiceControl.executeCommand("speed", "zero"));
        assertTrue(voiceControl.executeCommand("speed", "0"));

        assertTrue(voiceControl.executeCommand("speed", "one"));
        assertTrue(voiceControl.executeCommand("speed", "1"));

        assertTrue(voiceControl.executeCommand("speed", "two"));
        assertTrue(voiceControl.executeCommand("speed", "2"));

        assertTrue(voiceControl.executeCommand("speed", "three"));
        assertTrue(voiceControl.executeCommand("speed", "3"));

        assertTrue(voiceControl.executeCommand("speed", "four"));
        assertTrue(voiceControl.executeCommand("speed", "4"));

        assertTrue(voiceControl.executeCommand("speed", "five"));
        assertTrue(voiceControl.executeCommand("speed", "5"));

        assertTrue(voiceControl.executeCommand("speed", "to", "one"));
        assertTrue(voiceControl.executeCommand("speed", "one", "please"));
    }

    @Test
    public void executeCommand_stop() {
        doNothing().when(smartCar).setSpeed(anyInt());

        final SmartCarVoiceControl voiceControl = SmartCarVoiceControl.create(smartCar);

        assertFalse(voiceControl.executeCommand("turn"));
        assertFalse(voiceControl.executeCommand("turn", "unknown", "arguments"));

        assertTrue(voiceControl.executeCommand("turn", "left"));
        assertTrue(voiceControl.executeCommand("turn", "right"));

        assertTrue(voiceControl.executeCommand("turn", "to", "the", "left"));
        assertTrue(voiceControl.executeCommand("turn", "left", "please"));
    }

    @Test
    public void executeCommand_turn() {
        doNothing().when(smartCar).setSpeed(anyInt());

        final SmartCarVoiceControl voiceControl = SmartCarVoiceControl.create(smartCar);

        assertFalse(voiceControl.executeCommand("speed"));
    }

    @Test
    public void executeCommand_unknown() {
        final SmartCarVoiceControl voiceControl = SmartCarVoiceControl.create(smartCar);

        assertFalse(voiceControl.executeCommand("unknown"));
        assertFalse(voiceControl.executeCommand("unknown", "unknown", "arguments"));
    }
}
