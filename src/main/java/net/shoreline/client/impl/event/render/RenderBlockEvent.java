package net.shoreline.client.impl.event.render;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Getter
@RequiredArgsConstructor
@Cancelable
public class RenderBlockEvent extends Event
{
    private final BlockState state;
}
