package net.shoreline.client.impl.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.api.font.GlyphBuffer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.module.client.FontModule;

public class RenderModule extends Toggleable
{
    private final GlyphBuffer buffer = new GlyphBuffer();

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
            buffer.clear();
            buffer.addText(FontManager.FONT, text, 0.0f, 0.0f);
            buffer.offsetToTopLeft();
            buffer.draw(context, x, y);
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
            buffer.clear();
            buffer.addText(FontManager.FONT, text, 0.0f, 0.0f);
            buffer.recalculateBounds();
            return Math.round(buffer.maxX - buffer.minX);
        }

        return mc.textRenderer.getWidth(text);
    }
}
