package net.shoreline.client.impl.event.render.item;

import lombok.AllArgsConstructor;
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

    @AllArgsConstructor
    @Cancelable
    @Setter
    @Getter
    public static class EquipProgress  extends RenderHeldItemEvent
    {
        private final Hand hand;
        private float height;
    }

    @AllArgsConstructor
    @Cancelable
    @Setter
    @Getter
    public static class HandSwing extends RenderHeldItemEvent
    {
        private final Hand hand;
        private float swingProgress;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Scaling extends RenderHeldItemEvent
    {
        private final MatrixStack matrixStack;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Translation extends RenderHeldItemEvent
    {
        private final MatrixStack matrixStack;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Size extends RenderHeldItemEvent
    {
        private final MatrixStack matrixStack;
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
