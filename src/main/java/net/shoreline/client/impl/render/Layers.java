package net.shoreline.client.impl.render;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

public class Layers
{
    public static Identifier GLINT = Identifier.of("shoreline", "textures/shine.png");

    public static final RenderLayer.MultiPhase QUADS = RenderLayer.of(
            "shoreline_quads", 156, false, true, Pipelines.QUADS,
            RenderLayer.MultiPhaseParameters.builder().build(false));

    public static RenderLayer.MultiPhase QUADS_GLINT = RenderLayer.of(
            "shoreline_quads_glint", 1536, false, true, Pipelines.QUADS_GLINT,
            RenderLayer.MultiPhaseParameters.builder().texture(
                            new RenderPhase.Texture(GLINT, false))
                    .texturing(RenderPhases.GLINT)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .build(false));

    public static final RenderLayer.MultiPhase GUI = RenderLayer.of(
            "shoreline_gui", 156, false, true, RenderPipelines.GUI_TEXTURED,
            RenderLayer.MultiPhaseParameters.builder().build(false));

    public static final RenderLayer.MultiPhase DEBUG_LINES = RenderLayer.of(
            "shoreline_debug_lines", 1536, Pipelines.DEBUG_LINES,
            RenderLayer.MultiPhaseParameters.builder().build(false));

    public static final BiFunction<Identifier, Boolean, RenderLayer> ENTITY =
            Util.memoize((texture, affectsOutline) ->
            {
                RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(
                                new RenderPhase.Texture(texture, false))
                        .lightmap(RenderLayer.ENABLE_LIGHTMAP)
                        .overlay(RenderLayer.ENABLE_OVERLAY_COLOR)
                        .build(affectsOutline);
                return RenderLayer.of("shoreline_entity", 1536, true, false, Pipelines.ENTITY, multiPhaseParameters);
            });
}
