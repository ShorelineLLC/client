package net.shoreline.client.impl.module.impl;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.module.client.FontModule;

public class RenderModule extends Toggleable
{
    public RenderModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public RenderModule(final String name,
                        final String[] nameAliases,
                        final String description,
                        final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    protected void drawText(MatrixStack matrices, Text text, int x, int y)
    {
        drawText(matrices, text, x, y, -1);
    }

    protected void drawText(MatrixStack matrices, Text text, int x, int y, int color)
    {
        if (text.getString().isEmpty())
        {
            return;
        }

        if (FontModule.INSTANCE.isEnabled())
        {
            FontManager.FONT.drawString(matrices, text.getString(), x, y, color);
            return;
        }

        mc.textRenderer.draw(text, x, y, color, true, matrices.peek().getPositionMatrix(), mc.getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.SEE_THROUGH, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        mc.getBufferBuilders().getEntityVertexConsumers().draw();
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
