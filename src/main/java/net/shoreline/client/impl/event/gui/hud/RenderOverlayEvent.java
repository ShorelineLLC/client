package net.shoreline.client.impl.event.gui.hud;

import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.mixin.gui.hud.MixinInGameHud;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @see MixinInGameHud
 * @since 1.0
 */
public class RenderOverlayEvent extends Event
{
    //
    private final DrawContext context;


    /**
     * @param context
     */
    public RenderOverlayEvent(DrawContext context)
    {
        this.context = context;
    }

    /**
     * @return
     */
    public DrawContext getContext()
    {
        return context;
    }

    public static class Post extends RenderOverlayEvent
    {
        //
        private final float tickDelta;

        /**
         * @param context
         * @param tickDelta
         */
        public Post(DrawContext context, float tickDelta)
        {
            super(context);
            this.tickDelta = tickDelta;
        }

        /**
         * @return
         */
        public float getTickDelta()
        {
            return tickDelta;
        }
    }

    @Cancelable
    public static class StatusEffect extends RenderOverlayEvent
    {
        /**
         * @param context
         */
        public StatusEffect(DrawContext context)
        {
            super(context);
        }
    }

    @Cancelable
    public static class ItemName extends RenderOverlayEvent
    {
        //
        private int x, y;

        /**
         * @param context
         */
        public ItemName(DrawContext context)
        {
            super(context);
        }

        public boolean isUpdateXY()
        {
            return x != 0 && y != 0;
        }

        public int getX()
        {
            return x;
        }

        /**
         * @param x
         */
        public void setX(int x)
        {
            this.x = x;
        }

        public int getY()
        {
            return y;
        }

        /**
         * @param y
         */
        public void setY(int y)
        {
            this.y = y;
        }
    }

    @Cancelable
    public static class Fire extends RenderOverlayEvent
    {
        /**
         * @param context
         */
        public Fire(DrawContext context)
        {
            super(context);
        }
    }

    @Cancelable
    public static class Portal extends RenderOverlayEvent
    {
        /**
         * @param context
         */
        public Portal(DrawContext context)
        {
            super(context);
        }
    }

    @Cancelable
    public static class Water extends RenderOverlayEvent
    {
        /**
         * @param context
         */
        public Water(DrawContext context)
        {
            super(context);
        }
    }

    @Cancelable
    public static class Block extends RenderOverlayEvent
    {
        /**
         * @param context
         */
        public Block(DrawContext context)
        {
            super(context);
        }
    }

    @Cancelable
    public static class Spyglass extends RenderOverlayEvent
    {
        /**
         * @param context
         */
        public Spyglass(DrawContext context)
        {
            super(context);
        }
    }

    @Cancelable
    public static class Pumpkin extends RenderOverlayEvent
    {
        /**
         * @param context
         */
        public Pumpkin(DrawContext context)
        {
            super(context);
        }
    }

    @Cancelable
    public static class BossBar extends RenderOverlayEvent
    {
        /**
         * @param context
         */
        public BossBar(DrawContext context)
        {
            super(context);
        }
    }

    @Cancelable
    public static class Frostbite extends RenderOverlayEvent
    {
        /**
         * @param context
         */
        public Frostbite(DrawContext context)
        {
            super(context);
        }
    }

    @Cancelable
    public static class Hotbar extends RenderOverlayEvent
    {
        /**
         * @param context
         */
        public Hotbar(DrawContext context)
        {
            super(context);
        }
    }
}
