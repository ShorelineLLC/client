package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.Hand;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
public class SwingHandEvent extends Event
{
    private final Hand hand;
}
