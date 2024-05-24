package net.shoreline.client.impl.font;

import net.minecraft.client.render.VertexConsumerProvider;
import net.shoreline.client.api.font.FontRenderer;
import net.shoreline.client.api.font.Glyph;
import net.shoreline.client.api.font.GlyphVisitor;
import org.joml.Matrix4f;

import java.awt.*;

/**
 * @author xgraza
 * @since 1.0
 */
public final class AWTFontRenderer implements FontRenderer
{
    private final Font font;

    public AWTFontRenderer(final Font font, float size)
    {
        this.font = font.deriveFont(size);
    }

    @Override
    public void draw(Matrix4f matrix4f, String text, double x, double y, int color, boolean shadow) {
        char[] chars = text.toCharArray();
    }

    @Override
    public void draw(Matrix4f matrix4f, GlyphVisitor visitor, String text, double x, double y, int color, boolean shadow) {

    }

    @Override
    public void draw(Matrix4f matrix4f, GlyphVisitor visitor, VertexConsumerProvider vertexConsumers, String text, double x, double y, int color, boolean shadow) {

    }

    @Override
    public int drawGlyph(FontRenderer fontRenderer, char c, double x, double y, int color, boolean shadow) {
        return 0;
    }

    @Override
    public int getStringWidth(String text) {
        return 0;
    }

    @Override
    public Glyph getGlyph(char c) {
        return null;
    }

    @Override
    public Glyph[] getGlyphMap() {
        return new Glyph[0];
    }
}
