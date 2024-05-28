package net.shoreline.client.api.font;

/**
 * @author xgraza
 * @since 1.0
 * @param value
 * @param textureWidth
 * @param textureHeight
 * @param width
 * @param height
 */
public record Glyph(int textureWidth, int textureHeight, int width, int height, char value, GlyphCache owner)
{

}