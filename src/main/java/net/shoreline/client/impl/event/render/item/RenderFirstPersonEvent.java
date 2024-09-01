package net.shoreline.client.impl.event.render.item;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

public class RenderFirstPersonEvent extends Event
{
    public final Hand hand;
    public final ItemStack item;
    public final float equipProgress;
    public final MatrixStack matrices;

    public RenderFirstPersonEvent(Hand hand, ItemStack item, float equipProgress, MatrixStack matrices)
    {
        this.hand = hand;
        this.item = item;
        this.equipProgress = equipProgress;
        this.matrices = matrices;
    }

    @Cancelable
    public static class Head extends RenderFirstPersonEvent
    {
        public Head(Hand hand, ItemStack item, float equipProgress, MatrixStack matrices)
        {
            super(hand, item, equipProgress, matrices);
        }
    }
}
