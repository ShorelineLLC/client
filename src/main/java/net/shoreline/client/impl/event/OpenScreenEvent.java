package net.shoreline.client.impl.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.Screen;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
public class OpenScreenEvent extends Event
{
    private final Screen screen;
}
