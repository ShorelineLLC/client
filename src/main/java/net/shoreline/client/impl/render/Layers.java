package net.shoreline.client.impl.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

public class Layers
{
    public static Identifier GLINT = Identifier.of("shoreline", "textures/shine.png");

    public static RenderLayer.MultiPhase QUADS_GLINT = RenderLayer.of(
            "shoreline_quads_glint", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 1536, false, true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(Programs.GLINT)
                    .texture(new RenderPhase.Texture(GLINT, TriState.DEFAULT, false))
                    .texturing(RenderPhases.GLINT)
                    .transparency(RenderPhase.GLINT_TRANSPARENCY)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .depthTest(RenderPhase.DepthTest.ALWAYS_DEPTH_TEST)
                    .cull(RenderPhase.Cull.DISABLE_CULLING)
                    .build(false));

    public static final BiFunction<Identifier, Boolean, RenderLayer> ENTITY = Util.memoize((texture, affectsOutline) ->
    {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                .program(RenderLayer.ENTITY_CUTOUT_PROGRAM)
                .texture(new RenderPhase.Texture(texture, TriState.DEFAULT, false))
                .transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
                .cull(RenderLayer.DISABLE_CULLING)
                .lightmap(RenderLayer.ENABLE_LIGHTMAP)
                .overlay(RenderLayer.ENABLE_OVERLAY_COLOR)
                .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                .build(affectsOutline);

        return RenderLayer.of("shoreline_entity", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, true, true, multiPhaseParameters);
    });
}
