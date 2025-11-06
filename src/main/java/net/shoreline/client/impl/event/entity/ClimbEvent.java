package net.shoreline.client.impl.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Getter
@Cancelable
public class ClimbEvent extends Event
{
    private final Block block;
}
