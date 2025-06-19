package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.input.Input;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
public class InputMovementEvent extends Event
{
    private final Input input;
}
