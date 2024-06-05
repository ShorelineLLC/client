package net.shoreline.client.impl.event.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class VelocityMultiplierEvent extends Event
{
    //
    private final BlockState state;

    /**
     * @param state
     */
    public VelocityMultiplierEvent(BlockState state)
    {
        this.state = state;
    }

    /**
     * @return
     */
    public Block getBlock()
    {
        return state.getBlock();
    }
}
