package net.shoreline.client.impl.event.gui.hud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.eventbus.event.Event;

public class HudOverlayEvent extends Event
{
    @RequiredArgsConstructor
    @Getter
    public static class Post extends HudOverlayEvent
    {
        private final DrawContext context;
        private final float tickDelta;
    }
}
