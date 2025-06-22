package net.shoreline.client.impl.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import net.shoreline.client.mixin.accessor.AccessorDrawContext;

import java.util.function.BiConsumer;

public record DefaultGuiRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        ScreenRect scissorArea,
        ScreenRect bounds,
        BiConsumer<VertexConsumer, Float> vertices
) implements SimpleGuiElementRenderState {
    public DefaultGuiRenderState(RenderPipeline pipeline, TextureSetup ts, DrawContext context, ScreenRect bounds, BiConsumer<VertexConsumer, Float> vertices) {
        this(pipeline, ts, ((AccessorDrawContext) context).getScissorStack().peekLast(), bounds, vertices);
    }
    @Override
    public void setupVertices(VertexConsumer vertices, float depth) {
        this.vertices.accept(vertices, depth);
    }
}
