package net.shoreline.client.impl.event.gui.hud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import net.shoreline.client.util.text.TextUtil;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

public class RenderChatEvent extends Event
{
    @RequiredArgsConstructor
    @Cancelable
    @Getter
    @Setter
    public static class Background extends RenderChatEvent
    {
        private final DrawContext context;
        private final int x, y, width;
        private final int color;
    }

    @RequiredArgsConstructor
    @Cancelable
    @Getter
    @Setter
    public static class Text extends Event
    {
        private final ChatHudLine.Visible chatLine;
        private final DrawContext context;
        private final OrderedText text;
        private final int x, y;
        private final int color;
        private final int u;

        private int width;

        public String getString()
        {
            return TextUtil.parseString(text);
        }
    }
}
