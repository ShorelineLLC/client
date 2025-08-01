package net.shoreline.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.api.font.GlyphBuffer;
import net.shoreline.client.impl.module.client.FontModule;
import net.shoreline.client.mixin.accessor.AccessorDrawContext;
import org.joml.Matrix3x2fStack;

public abstract class DrawableComponent
{
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    protected final GlyphBuffer textBuffer = new GlyphBuffer();

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

    protected void drawTexturedRect(DrawContext context,
                                    Identifier sprite,
                                    int x,
                                    int y,
                                    int width,
                                    int height)
    {
        //context.drawTexturedQuad(sprite, x, y, x + width, y + height, 0.0f, 1.0f, 0.0f, 1.0f);
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
                            GlyphBuffer glyphBuffer,
                            Text text,
                            int x,
                            int y,
                            float offX,
                            float offY)
    {
        drawText(context, glyphBuffer, text, x, y, offX, offY, FontModule.INSTANCE.isEnabled());
    }

    protected void drawText(DrawContext context,
                            GlyphBuffer glyphBuffer,
                            Text text,
                            int x,
                            int y,
                            float offX,
                            float offY,
                            boolean customFont)
    {
        if (text.getString().isEmpty())
        {
            return;
        }

        if (customFont)
        {
            glyphBuffer.clear();
            glyphBuffer.addText(FontManager.FONT, text, offX, offY);
            glyphBuffer.offsetToTopLeft();
            glyphBuffer.draw(context, x + offX, y + offY);
            return;
        }

        context.drawText(mc.textRenderer, text, x, y, -1, true);
    }

    protected void drawText(DrawContext context,
                            GlyphBuffer glyphBuffer,
                            Text text,
                            int x,
                            int y,
                            boolean customFont)
    {
        drawText(context, glyphBuffer, text, x, y, 0.0f, 0.0f, customFont);
    }

    protected void drawText(DrawContext context,
                            GlyphBuffer glyphBuffer,
                            Text text,
                            int x,
                            int y)
    {
        drawText(context, glyphBuffer, text, x, y, 0.0f, 0.0f);
    }

    protected void enableScissor(DrawContext context, int x1, int y1, int x2, int y2)
    {
        context.enableScissor(x1, y1, x2, y2);
    }

    protected void disableScissor(DrawContext context)
    {
        context.disableScissor();
    }

    protected int getTextWidth(GlyphBuffer glyphBuffer, Text text)
    {
        return getTextWidth(glyphBuffer, text, FontModule.INSTANCE.isEnabled());
    }

    protected int getTextWidth(GlyphBuffer glyphBuffer, Text text, boolean customFont)
    {
        if (text.getString().isEmpty())
        {
            return 0;
        }

        if (customFont)
        {
            glyphBuffer.clear();
            glyphBuffer.addText(FontManager.FONT, text, 0.0f, 0.0f);
            glyphBuffer.recalculateBounds();
            return Math.round(glyphBuffer.maxX - glyphBuffer.minX);
        }

        return mc.textRenderer.getWidth(text);
    }
}
