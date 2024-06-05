package net.shoreline.client.impl.event.block;

import net.minecraft.block.Block;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class BlockSlipperinessEvent extends Event
{
    //
    private final Block block;
    private float slipperiness;

    /**
     * @param block
     * @param slipperiness
     */
    public BlockSlipperinessEvent(Block block, float slipperiness)
    {
        this.block = block;
        this.slipperiness = slipperiness;
    }

    /**
     * @return
     */
    public Block getBlock()
    {
        return block;
    }

    /**
     * @return
     */
    public float getSlipperiness()
    {
        return slipperiness;
    }

    /**
     * @param slipperiness
     */
    public void setSlipperiness(float slipperiness)
    {
        this.slipperiness = slipperiness;
    }
}
