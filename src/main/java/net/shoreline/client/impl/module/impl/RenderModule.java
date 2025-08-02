package net.shoreline.client.impl.module.impl;

import net.minecraft.client.gui.DrawContext;
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

    protected void drawText(DrawContext context, Text text, int x, int y)
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

        context.drawText(mc.textRenderer, text, x, y, -1, false);
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
