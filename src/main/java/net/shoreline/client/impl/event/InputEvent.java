package net.shoreline.client.impl.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shoreline.eventbus.Event;

public class InputEvent extends Event
{
    @RequiredArgsConstructor
    @Getter
    public static class Mouse extends InputEvent
    {
        private final int button;
        private final int action;
        private final int mods;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Keyboard extends InputEvent
    {
        private final int key;
        private final int scancode;
        private final int action;
        private final int modifiers;
    }
}
