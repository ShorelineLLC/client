package net.shoreline.client.impl.event.gui.hud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.eventbus.event.Event;

public class HudOverlayEvent extends Event
{
    @RequiredArgsConstructor
    public static class Post extends HudOverlayEvent
    {
        @Getter
        private final DrawContext context;

        @Getter
        private final float tickDelta;
    }
}
