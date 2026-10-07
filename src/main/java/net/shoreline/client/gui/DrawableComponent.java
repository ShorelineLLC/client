package net.shoreline.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.FontModule;

public abstract class DrawableComponent
{
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public abstract void drawComponent(DrawContext context,
                                       float mouseX,
                                       float mouseY,
                                       float delta);

    protected void drawRect(DrawContext context,
                            float x,
                            float y,
                            float width,
                            float height,
                            int color)
    {
        Managers.RENDER.drawRect(context, x, y, width, height, color);
    }

    protected void drawTexturedRect(DrawContext context,
                                    Identifier sprite,
                                    float x,
                                    float y,
                                    float width,
                                    float height,
                                    int color)
    {
        int rWidth = Math.round(width);
        int rHeight = Math.round(height);
        context.drawTexture(RenderLayer::getGuiTextured, sprite,
                Math.round(x), Math.round(y), 0.0f, 0.0f, rWidth, rHeight, rWidth, rHeight, color);
    }

    protected void drawOutline(DrawContext context,
                               float x,
                               float y,
                               float width,
                               float height,
                               float thickness,
                               int color)
    {
        Managers.RENDER.drawOutline(context, x, y, width, height, thickness, color);
    }

    protected void drawText(DrawContext context,
                            String text,
                            float x,
                            float y,
                            int color)
    {
        if (text.isEmpty())
        {
            return;
        }

        if (FontModule.INSTANCE.isEnabled())
        {
            FontManager.FONT_RENDERER.drawStringWithShadow(context, text, x, y, color);
            return;
        }

        context.drawTextWithShadow(mc.textRenderer, text, Math.round(x), Math.round(y), color);
    }

    protected void enableScissor(DrawContext context, float x1, float y1, float x2, float y2)
    {
        context.enableScissor((int) Math.floor(x1 - 1.0f), (int) Math.floor(y1), (int) Math.ceil(x2 + 1.0f), (int) Math.ceil(y2));
    }

    protected void disableScissor(DrawContext context)
    {
        context.disableScissor();
    }

    protected int getTextWidth(String text)
    {
        if (text.isEmpty())
        {
            return 0;
        }

        return FontModule.INSTANCE.isEnabled()
                ? FontManager.FONT_RENDERER.getStringWidth(text)
                : mc.textRenderer.getWidth(text);
    }
}
