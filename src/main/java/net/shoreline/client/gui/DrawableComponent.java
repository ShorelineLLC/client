package net.shoreline.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.impl.module.client.FontModule;

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

    protected void drawTexturedRect(DrawContext context,
                                    Identifier sprite,
                                    int x,
                                    int y,
                                    int width,
                                    int height)
    {
        context.drawTexture(RenderLayer::getGuiTextured, sprite, x, y, 0.0f, 0.0f, width, height, width, height, -1);
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
                            Text text,
                            int x,
                            int y)
    {
        if (text.getString().isEmpty())
        {
            return;
        }

        if (FontModule.INSTANCE.isEnabled())
        {
            FontManager.FONT.drawString(context.getMatrices(), text.getString(), x, y, -1);
            return;
        }

        context.drawText(mc.textRenderer, text, x, y, -1, true);
    }

    protected void enableScissor(DrawContext context, int x1, int y1, int x2, int y2)
    {
        context.enableScissor(x1, y1, x2, y2);
    }

    protected void disableScissor(DrawContext context)
    {
        context.disableScissor();
    }

    protected int getTextWidth(Text text)
    {
        if (text.getString().isEmpty())
        {
            return 0;
        }

        if (FontModule.INSTANCE.isEnabled())
        {
            return (int) FontManager.FONT.getStringWidth(text.getString());
        }

        return mc.textRenderer.getWidth(text);
    }
}
