package net.shoreline.client.impl.event.gui.hud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class PlayerListIconEvent extends Event
{
    @Cancelable
    public static class Width extends PlayerListIconEvent
    {
        private final String text;

        public Width(String text)
        {
            this.text = text;
        }

        public String getText()
        {
            return text;
        }
    }

    @Cancelable
    public static class Render extends PlayerListIconEvent
    {
        private final Text playerName;

        private final DrawContext context;
        private final TextRenderer textRenderer;
        private final double x;
        private final double y;
        private final int color;

        public Render(Text playerName, DrawContext context, TextRenderer textRenderer, double x, double y, int color)
        {
            this.context = context;
            this.textRenderer = textRenderer;
            this.playerName = playerName;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public DrawContext getContext()
        {
            return context;
        }

        public MatrixStack getMatrixStack()
        {
            return context.getMatrices();
        }

        public TextRenderer getTextRenderer()
        {
            return textRenderer;
        }

        public double getX()
        {
            return x;
        }

        public double getY()
        {
            return y;
        }

        public int getColor()
        {
            return color;
        }

        public Text getPlayerNameText()
        {
            return playerName;
        }
    }
}
