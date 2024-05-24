package net.shoreline.client.impl.event.keyboard;

import net.minecraft.client.input.Input;
import net.shoreline.eventbus.Cancelable;
import net.shoreline.eventbus.StageEvent;

@Cancelable
public class KeyboardTickEvent extends StageEvent {

    private final Input input;

    public KeyboardTickEvent(Input input) {
        this.input = input;
    }

    public Input getInput() {
        return input;
    }
}
