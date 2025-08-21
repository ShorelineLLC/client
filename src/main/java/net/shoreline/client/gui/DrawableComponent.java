package net.shoreline.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.impl.imixin.IDrawContext;
import net.shoreline.client.impl.module.client.FontModule;
import org.joml.Matrix4f;

public abstract class DrawableComponent
{
    protected final MinecraftClient mc = MinecraftClient.getInstance();

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
        width += x;
        height += y;
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        VertexConsumer vertexConsumer = ((IDrawContext) context).getVertexConsumerProvider().getBuffer(RenderLayer.getGui());
        vertexConsumer.vertex(matrix4f, x, y, 0).color(color);
        vertexConsumer.vertex(matrix4f, x, height, 0).color(color);
        vertexConsumer.vertex(matrix4f, width, height, 0).color(color);
        vertexConsumer.vertex(matrix4f, width, y, 0).color(color);
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
                            int x,
                            int y,
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

        context.drawTextWithShadow(mc.textRenderer, text, x, y, color);
    }

    protected void enableScissor(DrawContext context, int x1, int y1, int x2, int y2)
    {
        context.enableScissor(x1, y1, x2, y2);
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

        if (FontModule.INSTANCE.isEnabled())
        {
            return (int) FontManager.FONT.getStringWidth(text);
        }

        return mc.textRenderer.getWidth(text);
    }
}
