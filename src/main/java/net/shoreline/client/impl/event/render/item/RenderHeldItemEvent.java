package net.shoreline.client.impl.event.render.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

public class RenderHeldItemEvent extends Event
{
    @Cancelable
    public static class Pre extends RenderHeldItemEvent {}

    @RequiredArgsConstructor
    @Getter
    public static class FirstPerson extends RenderHeldItemEvent
    {
        private final MatrixStack matrixStack;
        private final Hand hand;
    }

    @Cancelable
    @Getter
    @Setter
    public static class Eating extends RenderHeldItemEvent
    {
        private float factorY;
        private int duration;
    }
}
