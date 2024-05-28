package net.shoreline.client.api.font;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.shoreline.client.mixin.accessor.AccessorNativeImage;
import net.shoreline.client.util.Globals;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class GlyphCache implements Globals
{
    private final char start, end;
    private final Font font;
    private final Identifier id;
    private final int padding;
    private int width, height;
    private final Char2ObjectArrayMap<Glyph> glyphs = new Char2ObjectArrayMap<>();
    private boolean generated;

    public GlyphCache(char from, char to, Font font, Identifier id, int padding)
    {
        this.start = from;
        this.end = to;
        this.font = font;
        this.id = id;
        this.padding = padding;
    }

    public Glyph getGlyph(char c) {
        if (!generated)
        {
            createBitmap();
        }
        return glyphs.get(c);
    }

    public void clear()
    {
        mc.getTextureManager().destroyTexture(id);
        glyphs.clear();
        generated = false;
    }

    public boolean contains(char c)
    {
        return c >= start && c < end;
    }

    public void createBitmap()
    {
        if (generated)
        {
            return;
        }
        List<Glyph> glyphs1 = new ArrayList<>();
        int range = end - start - 1;
        int ceiling = (int) (Math.ceil(Math.sqrt(range)) * 1.5);
        glyphs.clear();
        int cached = 0;
        int charX = 0;
        int maxX = 0, maxY = 0;
        int currentX = 0, currentY = 0;
        int currentRowMaxY = 0;
        AffineTransform af = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(af, true, false);
        while (cached <= range)
        {
            char currentChar = (char) (start + cached);
            Rectangle2D stringBounds = font.getStringBounds(String.valueOf(currentChar), frc);
            int width = (int) Math.ceil(stringBounds.getWidth());
            int height = (int) Math.ceil(stringBounds.getHeight());
            cached++;
            maxX = Math.max(maxX, currentX + width);
            maxY = Math.max(maxY, currentY + height);
            if (charX >= ceiling)
            {
                currentX = 0;
                currentY += currentRowMaxY + padding; // add height of highest glyph, and reset
                charX = 0;
                currentRowMaxY = 0;
            }
            currentRowMaxY = Math.max(currentRowMaxY, height); // calculate the highest glyph in this row
            glyphs1.add(new Glyph(currentX, currentY, width, height, currentChar, this));
            currentX += width + padding;
            charX++;
        }
        BufferedImage bufferedImage = new BufferedImage(Math.max(maxX + padding, 1), Math.max(maxY + padding, 1), BufferedImage.TYPE_INT_ARGB);
        width = bufferedImage.getWidth();
        height = bufferedImage.getHeight();
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setColor(new Color(255, 255, 255, 0));
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        for (Glyph glyph : glyphs1)
        {
            g2d.setFont(font);
            FontMetrics fontMetrics = g2d.getFontMetrics();
            g2d.drawString(String.valueOf(glyph.value()), glyph.textureWidth(), glyph.textureHeight() + fontMetrics.getAscent());
            glyphs.put(glyph.value(), glyph);
        }
        registerTexture(id, bufferedImage);
        generated = true;
    }

    public Identifier getId()
    {
        return id;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    // https://github.com/0x3C50/Renderer
    public void registerTexture(Identifier identifier, BufferedImage bufferedImage)
    {
        try
        {
            int imageWidth = bufferedImage.getWidth();
            int imageHeight = bufferedImage.getHeight();
            NativeImage image = new NativeImage(NativeImage.Format.RGBA, imageWidth, imageHeight, false);
            long ptr = ((AccessorNativeImage) (Object) image).hookGetPointer();
            IntBuffer backingBuffer = MemoryUtil.memIntBuffer(ptr, image.getWidth() * image.getHeight());
            WritableRaster raster = bufferedImage.getRaster();
            ColorModel colorModel = bufferedImage.getColorModel();
            int nbands = raster.getNumBands();
            int dataType = raster.getDataBuffer().getDataType();
            Object dataBuffer = switch (dataType) {
                case DataBuffer.TYPE_BYTE -> new byte[nbands];
                case DataBuffer.TYPE_USHORT -> new short[nbands];
                case DataBuffer.TYPE_INT -> new int[nbands];
                case DataBuffer.TYPE_FLOAT -> new float[nbands];
                case DataBuffer.TYPE_DOUBLE -> new double[nbands];
                default -> throw new IllegalArgumentException("Unknown data buffer type: " + dataType);
            };
            for (int y = 0; y < imageHeight; y++)
            {
                for (int x = 0; x < imageWidth; x++)
                {
                    raster.getDataElements(x, y, dataBuffer);
                    int a = colorModel.getAlpha(dataBuffer);
                    int r = colorModel.getRed(dataBuffer);
                    int g = colorModel.getGreen(dataBuffer);
                    int b = colorModel.getBlue(dataBuffer);
                    int argb = a << 24 | b << 16 | g << 8 | r;
                    backingBuffer.put(argb);
                }
            }
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            texture.upload();
            if (RenderSystem.isOnRenderThread())
            {
                mc.getTextureManager().registerTexture(identifier, texture);
            }
            else
            {
                RenderSystem.recordRenderCall(() -> mc.getTextureManager().registerTexture(identifier, texture));
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}
