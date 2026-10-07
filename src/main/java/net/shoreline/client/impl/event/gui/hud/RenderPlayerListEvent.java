package net.shoreline.client.impl.event.gui.hud;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class RenderPlayerListEvent extends Event
{
    private final DrawContext context;

    @Getter
    public static class Pre extends RenderPlayerListEvent
    {
        private final int x1, y1, x2, y2;

        public Pre(DrawContext context, int x1, int y1, int x2, int y2)
        {
            super(context);
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    public static class Post extends RenderPlayerListEvent {
        public Post(DrawContext context) {
            super(context);
        }
    }

    @RequiredArgsConstructor
    @Cancelable
    @Getter
    @Setter
    public static class DrawText extends Event
    {
        private final Text text;
        private int color;
    }

    @Getter
    @Setter
    @Cancelable
    public static class Collect extends Event
    {
        private List<PlayerListEntry> players;
    }
}
