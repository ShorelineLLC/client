package net.shoreline.client.impl.font;

import com.google.common.collect.Lists;
import net.minecraft.client.font.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.module.client.FontModule;
import net.shoreline.client.mixin.accessor.AccessorTextRenderer;
import net.shoreline.client.util.Globals;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Deprecated
public class VanillaTextRenderer implements Globals
{

    public static TextRenderer getTextRender(String fontName) throws IOException
    {
        List<Font.FontFilterPair> list = Lists.newArrayList();
        TrueTypeFontLoader loader = new TrueTypeFontLoader(
                Identifier.of(String.format("shoreline:%s.ttf", fontName)),
                11,
                20,
                TrueTypeFontLoader.Shift.NONE,
                ""
        );
        FontLoader.Loadable loadable = loader.build().orThrow();
        Font font = loadable.load(mc.getResourceManager());
        list.add(new Font.FontFilterPair(font, FontFilterType.FilterMap.NO_FILTER));
        FontStorage storage = new FontStorage(mc.getTextureManager(), Identifier.of("shoreline:tr"));
        storage.setFonts(list, Collections.emptySet());
        return new TextRenderer(id -> storage, true);
    }

    // Autism
    public void drawWithShadow(MatrixStack matrices, String text, float x, float y, int color)
    {
        draw(matrices, text, x + 1.0f, y + 1.0f, color, true);
        draw(matrices, text, x, y, color, false);
    }

    public void draw(MatrixStack matrices, String text, float x, float y, int color, boolean shadow)
    {
        this.draw(text, x, y, color, matrices.peek().getPositionMatrix(), shadow);
    }

    private void draw(String text, float x, float y, int color, Matrix4f matrix, boolean shadow)
    {
        if (text == null)
        {
            return;
        }
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(new BufferAllocator(1536));
        draw(text, x, y, color, shadow, matrix,
                immediate, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        immediate.draw();
    }

    public void draw(String text, float x, float y, int color, boolean shadow,
                     Matrix4f matrix, VertexConsumerProvider vertexConsumers,
                     TextRenderer.TextLayerType layerType, int backgroundColor, int light)
    {
        drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    private void drawInternal(String text, float x, float y, int color, boolean shadow,
                              Matrix4f matrix, VertexConsumerProvider vertexConsumers,
                              TextRenderer.TextLayerType layerType, int backgroundColor, int light)
    {
        // color = TextRenderer.tweakTransparency(color);
        Matrix4f matrix4f = new Matrix4f(matrix);
        drawLayer(text, x, y, color, shadow, matrix4f, vertexConsumers, layerType, backgroundColor, light);
    }

    private void drawLayer(String text, float x, float y, int color, boolean shadow,
                           Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider,
                           TextRenderer.TextLayerType layerType, int underlineColor, int light)
    {
        Drawer drawer = new Drawer(vertexConsumerProvider, x, y, color, shadow, matrix, layerType, light);
        TextVisitFactory.visitFormatted(text, Style.EMPTY, drawer);
        drawer.drawLayer();
    }

    public static class Drawer implements CharacterVisitor
    {
        final VertexConsumerProvider vertexConsumers;
        private final float brightnessMultiplier;
        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;
        private final Matrix4f matrix;
        private final TextRenderer.TextLayerType layerType;
        private final int light;
        float x;
        float y;
        @Nullable
        private List<GlyphRenderer.Rectangle> rectangles;

        public Drawer(VertexConsumerProvider vertexConsumers, float x,
                      float y, int color, boolean shadow, Matrix4f matrix,
                      TextRenderer.TextLayerType layerType, int light)
        {
            this.vertexConsumers = vertexConsumers;
            this.x = x;
            this.y = y;
            this.brightnessMultiplier = shadow ? 0.25f : 1.0f;
            this.red = (float) (color >> 16 & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.green = (float) (color >> 8 & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.blue = (float) (color & 0xFF) / 255.0f * this.brightnessMultiplier;
            this.alpha = (float) (color >> 24 & 0xFF) / 255.0f;
            this.matrix = matrix;
            this.layerType = layerType;
            this.light = light;
        }

        @Override
        public boolean accept(int i, Style style, int j)
        {
            // float n;
            float l;
            float h;
            float g;
            FontStorage fontStorage = ((AccessorTextRenderer) mc.textRenderer).hookGetFontStorage(style.getFont());
            Glyph glyph = fontStorage.getGlyph(j, ((AccessorTextRenderer) mc.textRenderer).hookGetValidateAdvance());
            GlyphRenderer glyphRenderer = style.isObfuscated() && j != 32 ? fontStorage.getObfuscatedGlyphRenderer(glyph) : fontStorage.getGlyphRenderer(j);
            boolean bl = style.isBold();
            float f = this.alpha;
            TextColor textColor = style.getColor();
            if (textColor != null)
            {
                int k = textColor.getRgb();
                g = (float) (k >> 16 & 0xFF) / 255.0f * this.brightnessMultiplier;
                h = (float) (k >> 8 & 0xFF) / 255.0f * this.brightnessMultiplier;
                l = (float) (k & 0xFF) / 255.0f * this.brightnessMultiplier;
            }
            else
            {
                g = this.red;
                h = this.green;
                l = this.blue;
            }
            if (!(glyphRenderer instanceof EmptyGlyphRenderer))
            {
                float m = bl ? glyph.getBoldOffset() : 0.0f;
                // n = this.shadow ? glyph.getShadowOffset() : 0.0f;
                VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(glyphRenderer.getLayer(this.layerType));
                ((AccessorTextRenderer) mc.textRenderer).hookDrawGlyph(glyphRenderer, bl, style.isItalic(), m, this.x, this.y, this.matrix, vertexConsumer, g, h, l, f, this.light);
            }
            float m = glyph.getAdvance(bl);
            this.x += m;
            return true;
        }

        public void drawLayer()
        {
            if (this.rectangles != null)
            {
                GlyphRenderer glyphRenderer = ((AccessorTextRenderer) mc.textRenderer).hookGetFontStorage(Style.DEFAULT_FONT_ID).getRectangleRenderer();
                VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(glyphRenderer.getLayer(this.layerType));
                for (GlyphRenderer.Rectangle rectangle : this.rectangles)
                {
                    glyphRenderer.drawRectangle(rectangle, this.matrix, vertexConsumer, this.light);
                }
            }
        }
    }
}
