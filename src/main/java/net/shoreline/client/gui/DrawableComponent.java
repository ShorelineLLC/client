package net.shoreline.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.impl.imixin.IDrawContext;
import net.shoreline.client.impl.module.client.FontModule;
import org.joml.Matrix4f;

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
        float x2 = x + width;
        float y2 = y + height;
        Matrix4f m = context.getMatrices().peek().getPositionMatrix();
        VertexConsumer vc = ((IDrawContext) context).getVertexConsumerProvider().getBuffer(RenderLayer.getGui());
        vc.vertex(m, x, y, 0).color(color);
        vc.vertex(m, x, y2, 0).color(color);
        vc.vertex(m, x2, y2, 0).color(color);
        vc.vertex(m, x2, y, 0).color(color);
    }

    protected void drawTexturedRect(DrawContext context,
                                    Identifier sprite,
                                    float x,
                                    float y,
                                    float width,
                                    float height)
    {
        int rWidth = Math.round(width);
        int rHeight = Math.round(height);
        context.drawTexture(RenderLayer::getGuiTextured, sprite,
                Math.round(x), Math.round(y), 0.0f, 0.0f, rWidth, rHeight, rWidth, rHeight, -1);
    }

    protected void drawOutline(DrawContext context,
                               float x,
                               float y,
                               float width,
                               float height,
                               float thickness,
                               int color)
    {
        float t2 = thickness * 2;
        drawRect(context, x - thickness, y - thickness, width + t2, thickness, color);
        drawRect(context, x - thickness, y, thickness, height, color);
        drawRect(context, x + width, y, thickness, height, color);
        drawRect(context, x - thickness, y + height, width + t2, thickness, color);
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
            FontManager.FONT.drawStringWithShadow(context, text, x, y, color);
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
                ? FontManager.FONT.getStringWidth(text)
                : mc.textRenderer.getWidth(text);
    }
}
