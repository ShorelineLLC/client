package net.shoreline.client.impl.module.impl;

import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;

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

    public void drawText(MatrixStack matrices, String text, float x, float y)
    {
        drawText(matrices, text, x, y, -1);
    }

    public void drawText(MatrixStack matrices, String text, float x, float y, int color)
    {
        Managers.RENDER.drawText(matrices, text, x, y, color);
    }

    public int getTextWidth(String text)
    {
        return (int) Managers.RENDER.getTextWidth(text);
    }
}
