package net.shoreline.client.impl.event.entity;

import net.minecraft.block.BlockState;
import net.shoreline.eventbus.Cancelable;
import net.shoreline.eventbus.Event;

@Cancelable
public class SlowMovementEvent extends Event {
    private final BlockState state;

    public SlowMovementEvent(BlockState state) {
        this.state = state;
    }

    public BlockState getState() {
        return state;
    }
}
