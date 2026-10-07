package net.shoreline.client.impl.event.gui.screen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

public class ChatScreenEvent extends Event
{
    @RequiredArgsConstructor
    @Getter
    @Cancelable
    public static class SendMessage extends ChatScreenEvent
    {
        private final String chatText;
    }
}
