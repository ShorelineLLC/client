package net.shoreline.client.impl.event.render;

import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class RenderShaderEvent extends Event
{
    private final MatrixStack matrices;
    private final float tickDelta;

    /**
     * @param matrices
     */
    public RenderShaderEvent(MatrixStack matrices, float tickDelta)
    {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }

    /**
     * @return
     */
    public MatrixStack getMatrices()
    {
        return matrices;
    }

    /**
     * @return
     */
    public float getTickDelta()
    {
        return tickDelta;
    }

    public static class BlockEntities extends RenderShaderEvent
    {
        /**
         * @param matrices
         * @param tickDelta
         */
        public BlockEntities(MatrixStack matrices, float tickDelta)
        {
            super(matrices, tickDelta);
        }
    }
}