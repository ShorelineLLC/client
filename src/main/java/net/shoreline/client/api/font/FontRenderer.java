package net.shoreline.client.api.font;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.impl.imixin.IDrawContext;
import net.shoreline.client.impl.inventory.SwapData;
import net.shoreline.client.impl.module.client.FontModule;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.misc.NameProtectModule;
import net.shoreline.client.impl.render.ColorUtil;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Matrix4f;

import java.awt.*;
import java.io.Closeable;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public final class FontRenderer implements Closeable
{
    @Getter
    private String name;
    private Font font;
    private final float size;

    private int scale;
    private int lastScale;

    private final ObjectList<GlyphCache> caches = new ObjectArrayList<>();
    private final Char2ObjectArrayMap<Glyph> glyphs = new Char2ObjectArrayMap<>();
    private final Map<Identifier, ObjectList<CharLocation>> cache = new Object2ObjectOpenHashMap<>();

    public FontRenderer(InputStream inputStream, float size)
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

    public FontRenderer(String name, float size)
    {
        this.name = name;
        this.font = new Font(name, Font.PLAIN, Math.round(size));
        this.size = size;
        createFont(font, size);
    }

    public static String stripControlCodes(String text) // TODO: do this with pattern again.
    {
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (c == '\u00A7' && i + 1 < text.length() && text.charAt(i + 1) == 'j')
            {
                i += 1;
                int remaining = text.length() - (i + 1);
                int skip = Math.min(8, remaining);
                i += skip;
                continue;
            }

            if (c == '\u00A7' && i + 1 < text.length())
            {
                i++;
                continue;
            }

            out.append(c);
        }

        return out.toString();
    }

    private void createFont(Font font, float size)
    {
        this.lastScale = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
        this.scale = this.lastScale;
        this.font = font.deriveFont(size * scale);
    }

    public void drawStringWithShadow(MatrixStack stack, String text, double x, double y, int color)
    {
        drawString(stack, null, text, x + 0.5f, y + 0.5f, color, true);
        drawString(stack, null, text, x, y, color, false);
    }

    public void drawString(MatrixStack stack, String text, double x, double y, int color)
    {
        drawString(stack, null, text, x, y, color, false);
    }

    public void drawStringWithShadow(DrawContext context, String text, double x, double y, int color)
    {
        drawString(context.getMatrices(), ((IDrawContext) context).getVertexConsumerProvider(), text, x + 0.5f, y + 0.5f, color, true);
        drawString(context.getMatrices(), ((IDrawContext) context).getVertexConsumerProvider(), text, x, y, color, false);
    }

    public void drawString(DrawContext context, String text, double x, double y, int color)
    {
        drawString(context.getMatrices(), ((IDrawContext) context).getVertexConsumerProvider(), text, x, y, color, false);
    }

    private void drawString(MatrixStack stack,
                            VertexConsumerProvider vertexConsumerProvider, String text, double x, double y, int color, boolean shadow)
    {
        float brightnessMultiplier = shadow ? 0.25f : 1.0f;
        float r = ((color >> 16) & 0xff) / 255.0f * brightnessMultiplier;
        float g = ((color >> 8) & 0xff) / 255.0f * brightnessMultiplier;
        float b = ((color) & 0xff) / 255.0f * brightnessMultiplier;
        float a = ((color >> 24) & 0xff) / 255.0f;
        drawString(stack, vertexConsumerProvider, text, (float) x, (float) y, r, g, b, a, brightnessMultiplier);
    }

    public void drawString(MatrixStack stack,
                           VertexConsumerProvider vertexConsumerProvider,
                           String text,
                           float x,
                           float y,
                           float r,
                           float g,
                           float b,
                           float a,
                           float brightnessMultiplier)
    {
        if (NameProtectModule.INSTANCE.isEnabled())
        {
            String username = MinecraftClient.getInstance().getSession().getUsername();
            if (text.contains(username))
            {
                text = text.replace(username, NameProtectModule.INSTANCE.getAliasConfig().getValue());
            }
        }

        int currentScale = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
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
        stack.scale(1.0f / scale, 1.0f / scale, 1.0f);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        char[] chars = text.toCharArray();
        float xOffset = 0;
        float yOffset = 0;
        boolean formatting = false;
        int lineStart = 0;

        glyphs.clear();
        synchronized (cache)
        {
            Mutable<Integer> index = new MutableObject<>(0);
            for (; index.getValue() < chars.length; index.setValue(index.getValue() + 1))
            {
                char c = chars[index.getValue()];
                if (formatting)
                {
                    formatting = false;
                    if (c == 'r')
                    {
                        r2 = r;
                        g2 = g;
                        b2 = b;
                    } else
                    {
                        int colorCode = getColorFromCode(text, index, c);
                        int[] col = ColorUtil.getRGBColorValues(colorCode);
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

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            for (Identifier identifier : cache.keySet())
            {
                List<CharLocation> locations = cache.get(identifier);

                if (FontModule.INSTANCE.getAntiAlias().getValue())
                {
                    AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(identifier);
                    if (texture != null)
                    {
                        texture.setFilter(true, true);
                    }
                }

                RenderSystem.setShaderTexture(0, identifier);
                drawGlyphs(stack, vertexConsumerProvider, locations, identifier, a);
            }

            RenderSystem.disableBlend();

            cache.clear();
        }

        stack.pop();
    }

    private void drawGlyphs(MatrixStack matrixStack,
                            VertexConsumerProvider vertexConsumerProvider,
                            List<CharLocation> locations,
                            Identifier identifier,
                            float opacity)
    {
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder;
        if (vertexConsumerProvider == null)
        {
            bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        } else
        {
            bufferBuilder = (BufferBuilder) vertexConsumerProvider.getBuffer(RenderLayer.getGuiTextured(identifier));
        }

        for (CharLocation charLocation : locations)
        {
            float xo = charLocation.x;
            float yo = charLocation.y;
            float cr = charLocation.r;
            float cg = charLocation.g;
            float cb = charLocation.b;
            Glyph glyph = charLocation.glyph;
            GlyphCache owner = glyph.owner();
            float w = glyph.width();
            float h = glyph.height();
            float u1 = (float) glyph.textureWidth() / owner.getWidth();
            float v1 = (float) glyph.textureHeight() / owner.getHeight();
            float u2 = (float) (glyph.textureWidth() + glyph.width()) / owner.getWidth();
            float v2 = (float) (glyph.textureHeight() + glyph.height()) / owner.getHeight();
            bufferBuilder.vertex(matrix4f, xo + 0, yo + h, 0).texture(u1, v2).color(cr, cg, cb, opacity);
            bufferBuilder.vertex(matrix4f, xo + w, yo + h, 0).texture(u2, v2).color(cr, cg, cb, opacity);
            bufferBuilder.vertex(matrix4f, xo + w, yo + 0, 0).texture(u2, v1).color(cr, cg, cb, opacity);
            bufferBuilder.vertex(matrix4f, xo + 0, yo + 0, 0).texture(u1, v1).color(cr, cg, cb, opacity);
        }

        if (vertexConsumerProvider == null)
        {
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        }
    }

    public int getStringWidth(String text)
    {
        if (NameProtectModule.INSTANCE.isEnabled())
        {
            String username = MinecraftClient.getInstance().getSession().getUsername();
            if (text.contains(username))
            {
                text = text.replace(username, NameProtectModule.INSTANCE.getAliasConfig().getValue());
            }
        }

        char[] c = stripControlCodes(text).toCharArray();
        float currentLine = 0;
        float maxPreviousLines = 0;
        boolean formatting = false;
        for (char c1 : c)
        {
            if (c1 == '\n')
            {
                maxPreviousLines = Math.max(currentLine, maxPreviousLines);
                currentLine = 0;
                continue;
            }

            if (formatting)
            {
                formatting = false;
                continue;
            }

            if (c1 == '§')
            {
                formatting = true;
                continue;
            }

            Glyph glyph = glyphs.computeIfAbsent(c1, g1 -> getGlyphFromChar(g1));
            float w = glyph == null ? 0 : glyph.width();
            currentLine += w / (float) this.scale;
        }

        return Math.round(Math.max(currentLine, maxPreviousLines));
    }

    public float getFontHeight()
    {
        return size;
    }

    public Glyph getGlyphFromChar(char c)
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
                FontModule.INSTANCE.getAntiAlias().getValue(), FontModule.INSTANCE.getFractionalMetrics().getValue());

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
        return Identifier.of(ShorelineMod.MOD_ID, "font/storage/" + generateRandomHex(32));
    }

    private static final String HEX_CHARS = "0123456789abcdef";

    public String generateRandomHex(int length)
    {
        StringBuilder hexString = new StringBuilder(length);
        for (int i = 0; i < length; i++)
        {
            int index = ThreadLocalRandom.current().nextInt(HEX_CHARS.length());
            hexString.append(HEX_CHARS.charAt(index));
        }
        return hexString.toString();
    }

    private int getColorFromCode(String str, Mutable<Integer> index, char code)
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
            case 'g' -> ThemeModule.INSTANCE.getPrimaryColor().getRGB();
            case 'h' -> SocialsModule.INSTANCE.getFriendsColor().getRGB();
            case 'j' ->
            {
                int start  = index.getValue() + 2;
                int end    = Math.min(start + 8, str.length());
                String hex = str.substring(start, end);
                try
                {
                    index.setValue(end - 2);
                    yield (int) Long.parseLong(hex, 16);
                }
                catch (NumberFormatException ignored)
                {
                }

                yield 0xFFFFFFFF;
            }

            default -> -1;
        };
    }

    public record CharLocation(float x, float y, float r, float g, float b, Glyph glyph) {}
}