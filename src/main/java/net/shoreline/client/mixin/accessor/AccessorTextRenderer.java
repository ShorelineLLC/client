package net.shoreline.client.mixin.accessor;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author Shoreline
 * @since 1.0
 */
@Mixin(TextRenderer.class)
public interface AccessorTextRenderer
{
    /**
     * @return
     */
    @Accessor("validateAdvance")
    boolean hookGetValidateAdvance();

    /**
     * @param id
     * @return
     */
    @Invoker("getFontStorage")
    FontStorage hookGetFontStorage(Identifier id);

    /**
     * @param glyphRenderer
     * @param bold
     * @param italic
     * @param weight
     * @param x
     * @param y
     * @param matrix
     * @param vertexConsumer
     * @param red
     * @param green
     * @param blue
     * @param alpha
     * @param light
     */
    @Invoker("drawGlyph")
    void hookDrawGlyph(GlyphRenderer glyphRenderer, boolean bold, boolean italic,
                       float weight, float x, float y, Matrix4f matrix,
                       VertexConsumer vertexConsumer, float red, float green,
                       float blue, float alpha, int light);

    /**
     * @param text
     * @param x
     * @param y
     * @param color
     * @param shadow
     * @param matrix
     * @param vertexConsumerProvider
     * @param layerType
     * @param underlineColor
     * @param light
     * @return
     */
    @Invoker("drawLayer")
    float hookDrawLayer(String text, float x, float y,
                        int color, boolean shadow, Matrix4f matrix,
                        VertexConsumerProvider vertexConsumerProvider,
                        TextRenderer.TextLayerType layerType, int underlineColor,
                        int light);
}
