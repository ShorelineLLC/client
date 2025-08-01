package net.shoreline.client.api.font;

import lombok.Getter;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.shoreline.client.mixin.accessor.AccessorNativeImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Glyph_Metrics;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.util.freetype.FreeType.*;

public class GlyphPage implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(GlyphPage.class);
    private final Font font;
    private final int glyphStart;
    private final int glyphEnd;

    @Override
    public void close() {
        tex.close();
    }

    public record GlyphMetrics(long width, long height, long hbX, long hbY, long hbA) {
    }

    public record Glyph(int id, int bitmapWidth, int bitmapHeight, int drawOffsetX, int drawOffsetY, AtomicInteger x,
                        AtomicInteger y, GlyphMetrics metrics) {
    }

    Glyph[] glyphs;
    public NativeImageBackedTexture tex;

    public GlyphPage(Font font, int glyphStart, int glyphEnd) {
        glyphs = new Glyph[glyphEnd - glyphStart];
        this.font = font;
        this.glyphStart = glyphStart;
        this.glyphEnd = glyphEnd;
        log.info("init page: {} {}-{}", font.toString(), glyphStart, glyphEnd);
        fill();
    }

    @Getter
    private int texWidth, texHeight;

    public Glyph getGlyph(int n) {
        return glyphs[n - glyphStart];
    }

    private void layout(int rowWidth) {
        int w = 0, pX = 0, pY = 0;
        Iterator<Glyph> iterator = Arrays.stream(glyphs).sorted(Comparator.comparingInt(it -> it.bitmapHeight)).iterator();
        int rowHeight = 0;
        while (iterator.hasNext()) {
            Glyph glyph = iterator.next();
            glyph.x.set(pX);
            glyph.y.set(pY);
            pX += glyph.bitmapWidth;
            rowHeight = Math.max(rowHeight, glyph.bitmapHeight);

            if (pX >= rowWidth) {
                pY += rowHeight;
                rowHeight = 0;
                w = Math.max(w, pX);
                pX = 0;
            }
        }

        pY += rowHeight;

        texWidth = Math.max(w, pX);
        texHeight = pY;
    }


    private void fill() {

    }
}
