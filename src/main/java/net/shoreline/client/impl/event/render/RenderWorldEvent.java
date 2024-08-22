package net.shoreline.client.impl.event.render;

import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @since 1.0
 */
public class RenderWorldEvent extends Event
{
    //
    private final MatrixStack matrices;
    private final float tickDelta;

    /**
     * @param matrices
     */
    public RenderWorldEvent(MatrixStack matrices, float tickDelta)
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

    public static class Game extends RenderWorldEvent
    {

        /**
         * @param matrices
         * @param tickDelta
         */
        public Game(MatrixStack matrices, float tickDelta)
        {
            super(matrices, tickDelta);
        }
    }

    public static class Hand extends RenderWorldEvent
    {
        /**
         * @param matrices
         * @param tickDelta
         */
        public Hand(MatrixStack matrices, float tickDelta)
        {
            super(matrices, tickDelta);
        }
    }
}
