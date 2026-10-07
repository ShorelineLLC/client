package net.shoreline.client.impl.event.gui.hud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

public class HudOverlayEvent extends Event
{
    @RequiredArgsConstructor
    @Getter
    public static class Post extends HudOverlayEvent
    {
        private final DrawContext context;
        private final float tickDelta;
    }

    @Cancelable
    public static class Potions extends HudOverlayEvent {}

    @Cancelable
    public static class ItemName extends HudOverlayEvent {}

    @RequiredArgsConstructor
    @Cancelable
    @Getter
    public static class Crosshair extends HudOverlayEvent
    {
        private final DrawContext context;
    }
}
