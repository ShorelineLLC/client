package net.shoreline.client.impl.font;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.font.Glyph;
import net.shoreline.client.api.font.GlyphCache;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.impl.module.client.FontModule;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.math.HexRandom;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.Closeable;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class AWTFontRenderer implements Closeable, Globals
{
    private Font font;
    private final float size;

    private int scale;
    private int lastScale;

    private static final Pattern PATTERN_CONTROL_CODE = Pattern.compile("(?i)\\u00A7[0-9A-FK-OG]");

    private final ObjectList<GlyphCache> caches = new ObjectArrayList<>();
    private final Char2ObjectArrayMap<Glyph> glyphs = new Char2ObjectArrayMap<>();
    private final Map<Identifier, ObjectList<CharLocation>> cache = new Object2ObjectOpenHashMap<>();

    public AWTFontRenderer(InputStream inputStream, float size)
    {
        try
        {
            this.font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            this.font = new Font("Verdana", Font.PLAIN, Math.round(size));
        }
        this.size = size;
        createFont(font, size);
    }

    public static String stripControlCodes(String text)
    {
        return PATTERN_CONTROL_CODE.matcher(text).replaceAll("");
    }

    private void createFont(Font font, float size)
    {
        this.lastScale = (int) mc.getWindow().getScaleFactor();
        this.scale = this.lastScale;
        this.font = font.deriveFont(size * scale);
    }

    public void drawStringWithShadow(MatrixStack stack, String text, double x, double y, int color)
    {
        drawString(stack, text, x + 0.5f, y + 0.5f, color, true);
        drawString(stack, text, x, y, color, false);
    }

    public void drawString(MatrixStack stack, String text, double x, double y, int color)
    {
        drawString(stack, text, x, y, color, false);
    }

    private void drawString(MatrixStack stack, String text, double x, double y, int color, boolean shadow)
    {
        float brightnessMultiplier = shadow ? 0.25f : 1.0f;
        float r = ((color >> 16) & 0xff) / 255.0f * brightnessMultiplier;
        float g = ((color >> 8) & 0xff) / 255.0f * brightnessMultiplier;
        float b = ((color) & 0xff) / 255.0f * brightnessMultiplier;
        float a = ((color >> 24) & 0xff) / 255.0f;
        drawString(stack, text, (float) x, (float) y, r, g, b, a, brightnessMultiplier);
    }

    public void drawString(MatrixStack stack, String text, double x, double y, Color color)
    {
        drawString(stack, text, x, y, color, false);
    }

    public void drawString(MatrixStack stack, String text, double x, double y, Color color, boolean shadow)
    {
        float brightnessMultiplier = shadow ? 0.25f : 1.0f;
        drawString(stack, text, (float) x, (float) y, color.getRed() / 255.0f * brightnessMultiplier, color.getGreen() / 255.0f * brightnessMultiplier, color.getBlue() / 255.0f * brightnessMultiplier, color.getAlpha(), brightnessMultiplier);
    }

    public void drawString(MatrixStack stack, String text, float x, float y, float r, float g, float b, float a, float brightnessMultiplier)
    {
        int currentScale = (int) mc.getWindow().getScaleFactor();
        if (currentScale != lastScale)
        {
            close();
            createFont(font, size);
        }
        float r2 = r;
        float g2 = g;
        float b2 = b;
        stack.push();
        y -= 3.0f;
        stack.translate(x, y, 0.0f);
        stack.scale(1.0f / scale, 1.0f / scale, 0.0f);

        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        Matrix4f matrix4f = stack.peek().getPositionMatrix();
        char[] chars = text.toCharArray();
        float xOffset = 0;
        float yOffset = 0;
        boolean formatting = false;
        int lineStart = 0;
        glyphs.clear();
        synchronized (cache)
        {
            for (int i = 0; i < chars.length; i++)
            {
                char c = chars[i];
                if (formatting)
                {
                    formatting = false;
                    if (c == 'r')
                    {
                        r2 = r;
                        g2 = g;
                        b2 = b;
                    }
                    else
                    {
                        int colorCode = getColorFromCode(c);
                        int[] col = toRgbComponents(colorCode);
                        r2 = col[0] / 255.0f * brightnessMultiplier;
                        g2 = col[1] / 255.0f * brightnessMultiplier;
                        b2 = col[2] / 255.0f * brightnessMultiplier;
                    }
                    continue;
                }
                if (c == '§')
                {
                    formatting = true;
                    continue;
                }
                else if (c == '\n')
                {
                    yOffset += getStringHeight(text.substring(lineStart, i)) * scale;
                    xOffset = 0;
                    lineStart = i + 1;
                    continue;
                }
                Glyph glyph = glyphs.computeIfAbsent(c, g1 -> getGlyphFromChar(g1));
                if (glyph != null)
                {
                    if (glyph.value() != ' ')
                    {
                        Identifier i1 = glyph.owner().getId();
                        CharLocation entry = new CharLocation(xOffset, yOffset, r2, g2, b2, glyph);
                        cache.computeIfAbsent(i1, integer -> new ObjectArrayList<>()).add(entry);
                    }
                    xOffset += glyph.width();
                }
            }
            for (Identifier identifier : cache.keySet())
            {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                try
                {
                    RenderSystem.setShaderTexture(0, identifier);
                }
                catch (Exception e)
                {
                    continue;
                }

                List<CharLocation> objects = cache.get(identifier);

                BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                for (CharLocation object : objects)
                {
                    float xo = object.x;
                    float yo = object.y;
                    float cr = object.r;
                    float cg = object.g;
                    float cb = object.b;
                    Glyph glyph = object.glyph;
                    GlyphCache owner = glyph.owner();
                    float w = glyph.width();
                    float h = glyph.height();
                    float u1 = (float) glyph.textureWidth() / owner.getWidth();
                    float v1 = (float) glyph.textureHeight() / owner.getHeight();
                    float u2 = (float) (glyph.textureWidth() + glyph.width()) / owner.getWidth();
                    float v2 = (float) (glyph.textureHeight() + glyph.height()) / owner.getHeight();
                    bufferBuilder.vertex(matrix4f, xo + 0, yo + h, 0).color(cr, cg, cb, a).texture(u1, v2);
                    bufferBuilder.vertex(matrix4f, xo + w, yo + h, 0).color(cr, cg, cb, a).texture(u2, v2);
                    bufferBuilder.vertex(matrix4f, xo + w, yo + 0, 0).color(cr, cg, cb, a).texture(u2, v1);
                    bufferBuilder.vertex(matrix4f, xo + 0, yo + 0, 0).color(cr, cg, cb, a).texture(u1, v1);
                }
                BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                RenderSystem.disableBlend();
            }

            cache.clear();
        }
        stack.pop();
    }

    public void drawCenteredString(MatrixStack stack, String text, double x, double y, int color)
    {
        drawString(stack, text, x, y, color, false);
    }

    public void drawCenteredString(MatrixStack stack, String text, double x, double y, int color, boolean shadow)
    {
        float brightnessMultiplier = shadow ? 0.25f : 1.0f;
        float r = ((color >> 16) & 0xff) / 255.0f * brightnessMultiplier;
        float g = ((color >> 8) & 0xff) / 255.0f * brightnessMultiplier;
        float b = ((color) & 0xff) / 255.0f * brightnessMultiplier;
        float a = (color & 0xff000000) != 0xff000000 ? 1.0f : ((color >> 24) & 0xff) / 255.0f * brightnessMultiplier;
        drawString(stack, text, (float) (x - getStringWidth(text) / 2f), (float) y, r, g, b, a, brightnessMultiplier);
    }

    public void drawCenteredString(MatrixStack stack, String text, double x, double y, Color color)
    {
        drawString(stack, text, x, y, color, false);
    }

    public void drawCenteredString(MatrixStack stack, String text, double x, double y, Color color, boolean shadow)
    {
        float brightnessMultiplier = shadow ? 0.25f : 1.0f;
        drawString(stack, text, (float) (x - getStringWidth(text) / 2.0f), (float) y, color.getRed() / 255.0f * brightnessMultiplier, color.getGreen() / 255.0f * brightnessMultiplier, color.getBlue() / 255.0f * brightnessMultiplier, color.getAlpha() / 255.0f, brightnessMultiplier);
    }

    public float getStringWidth(String text)
    {
        char[] c = stripControlCodes(text).toCharArray();
        float currentLine = 0;
        float maxPreviousLines = 0;
        for (char c1 : c)
        {
            if (c1 == '\n')
            {
                maxPreviousLines = Math.max(currentLine, maxPreviousLines);
                currentLine = 0;
                continue;
            }
            Glyph glyph = glyphs.computeIfAbsent(c1, g1 -> getGlyphFromChar(g1));
            float w = glyph == null ? 0 : glyph.width();
            currentLine += w / (float) this.scale;
        }
        return Math.max(currentLine, maxPreviousLines);
    }

    public float getStringHeight(String text)
    {
        char[] c = stripControlCodes(text).toCharArray();
        if (c.length == 0)
        {
            c = new char[]{' '};
        }
        float currentLine = 0;
        float previous = 0;
        for (char c1 : c)
        {
            if (c1 == '\n')
            {
                if (currentLine == 0)
                {
                    Glyph glyph = glyphs.computeIfAbsent(' ', g1 -> getGlyphFromChar(g1));
                    currentLine = glyph.height() / (float) scale;
                }
                previous += currentLine;
                currentLine = 0;
                continue;
            }
            Glyph glyph = glyphs.computeIfAbsent(c1, g1 -> getGlyphFromChar(g1));
            float h = glyph == null ? 0 : glyph.height();
            currentLine = Math.max(h / (float) scale, currentLine);
        }
        return currentLine + previous;
    }

    public float getFontHeight()
    {
        return size;
    }

    private Glyph getGlyphFromChar(char c)
    {
        // Return cached glyph
        for (GlyphCache map : caches)
        {
            if (map.contains(c))
            {
                return map.getGlyph(c);
            }
        }
        int base = 256 * (int) Math.floor((double) c / (double) 256);
        GlyphCache glyphCache = new GlyphCache((char) base, (char) (base + 256), font, getGlyphIdentifier(), 5,
                FontModule.getInstance().getAntiAlias(), FontModule.getInstance().getFractionalMetrics());
        caches.add(glyphCache);
        return glyphCache.getGlyph(c);
    }

    @Override
    public void close()
    {
        try
        {
            for (GlyphCache cache1 : caches)
            {
                cache1.clear();
            }
            caches.clear();
            glyphs.clear();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Identifier getGlyphIdentifier()
    {
        return Identifier.of("shoreline", "font/storage/" + HexRandom.generateRandomHex(32));
    }

    public int[] toRgbComponents(int color)
    {
        float r = (color >> 16) & 0xff;
        float g = (color >> 8) & 0xff;
        float b = (color) & 0xff;
        float a = (color & 0xff000000) != 0xff000000 ? 255.0f : (color >> 24) & 0xff;
        return new int[]{(int) r, (int) g, (int) b, (int) a};
    }

    private int getColorFromCode(char code)
    {
        return switch (code)
        {
            case '0' -> Color.BLACK.getRGB();
            case '1' -> 0xff0000AA;
            case '2' -> 0xff00AA00;
            case '3' -> 0xff00AAAA;
            case '4' -> 0xffAA0000;
            case '5' -> 0xffAA00AA;
            case '6' -> 0xffFFAA00;
            case '7' -> 0xffAAAAAA;
            case '8' -> 0xff555555;
            case '9' -> 0xff5555FF;
            case 'a' -> 0xff55FF55;
            case 'b' -> 0xff55FFFF;
            case 'c' -> 0xffFF5555;
            case 'd' -> 0xffFF55FF;
            case 'e' -> 0xffFFFF55;
            case 'f' -> 0xffffffff;
            case 's' -> ColorsModule.getInstance().getRGB();
            case 'g' -> SocialsModule.getInstance().getFriendRGB();
            default -> -1;
        };
    }

    public record CharLocation(float x, float y, float r, float g, float b, Glyph glyph)
    {
    }
}