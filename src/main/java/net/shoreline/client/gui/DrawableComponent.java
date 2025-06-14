package net.shoreline.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class DrawableComponent
{
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public abstract void drawComponent(DrawContext context,
                                       float mouseX,
                                       float mouseY,
                                       float delta);

    protected void drawRect(DrawContext context,
                            int x,
                            int y,
                            int width,
                            int height,
                            int color)
    {
        context.fill(x, y, x + width, y + height, color);
    }

    protected void drawOutline(DrawContext context,
                               int x,
                               int y,
                               int width,
                               int height,
                               int thickness,
                               int color)
    {
        int t2 = thickness * 2;
        drawRect(context, x - thickness, y - thickness, width + t2, thickness, color);
        drawRect(context, x - thickness, y, thickness, height, color);
        drawRect(context, x + width, y, thickness, height, color);
        drawRect(context, x - thickness, y + height, width + t2, thickness, color);
    }

    protected void drawText(DrawContext context,
                            String text,
                            int x,
                            int y,
                            int color,
                            boolean shadow)
    {
        context.drawText(mc.textRenderer, text, x, y, color, shadow);
    }

    protected void drawText(DrawContext context,
                            String text,
                            int x,
                            int y,
                            int color)
    {
        context.drawText(mc.textRenderer, text, x, y, color, true);
    }
}
