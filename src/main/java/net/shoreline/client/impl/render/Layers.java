package net.shoreline.client.impl.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.shoreline.client.ShorelineMod;

import java.util.OptionalDouble;
import java.util.function.BiFunction;

public class Layers
{
    public static Identifier GLINT = Identifier.of(ShorelineMod.MOD_ID, "textures/shine.png");

    public static final RenderLayer.MultiPhase QUADS_GLINT = RenderLayer.of(
            "shoreline_quads_glint", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 1536, false, true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(Programs.GLINT_PROGRAM)
                    .texture(new RenderPhase.Texture(GLINT, TriState.DEFAULT, false))
                    .texturing(RenderPhases.GLINT)
                    .transparency(RenderPhase.GLINT_TRANSPARENCY)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .depthTest(RenderPhase.DepthTest.ALWAYS_DEPTH_TEST)
                    .cull(RenderPhase.Cull.DISABLE_CULLING)
                    .build(false));

    public static final RenderLayer.MultiPhase QUADS = RenderLayer.of(
            "shoreline_quads", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 1536, false, true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.POSITION_COLOR_PROGRAM)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .depthTest(RenderPhase.DepthTest.ALWAYS_DEPTH_TEST)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .build(false));

    public static final RenderLayer.MultiPhase LINES = RenderLayer.of(
            "shoreline_lines", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES, 1536, false, true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.POSITION_COLOR_PROGRAM)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(1.5)))
                    .depthTest(RenderPhase.DepthTest.ALWAYS_DEPTH_TEST)
                    .cull(RenderPhase.Cull.DISABLE_CULLING)
                    .build(false));

    public static final BiFunction<Identifier, Boolean, RenderLayer> ENTITY = Util.memoize((texture, affectsOutline) ->
    {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                .program(RenderLayer.ENTITY_CUTOUT_PROGRAM)
                .texture(new RenderPhase.Texture(texture, TriState.DEFAULT, false))
                .transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
                .lightmap(RenderLayer.ENABLE_LIGHTMAP)
                .overlay(RenderLayer.ENABLE_OVERLAY_COLOR)
                .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                .cull(RenderPhase.Cull.DISABLE_CULLING)
                .build(affectsOutline);

        return RenderLayer.of("shoreline_entity", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, true, true, multiPhaseParameters);
    });

    public static final BiFunction<Identifier, Boolean, RenderLayer> CRYSTALS = Util.memoize((texture, affectsOutline) ->
    {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                .program(RenderPhase.ENTITY_CUTOUT_NONULL_PROGRAM)
                .texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
                .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                .cull(RenderPhase.Cull.DISABLE_CULLING)
                .lightmap(RenderLayer.ENABLE_LIGHTMAP)
                .overlay(RenderLayer.ENABLE_OVERLAY_COLOR)
                .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                .build(affectsOutline);
        return RenderLayer.of("entity_cutout_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, true, true, multiPhaseParameters);
    });

    public static final RenderLayer SNOW = RenderLayer.of(
            "shoreline_snow",
            VertexFormats.POSITION_TEXTURE_COLOR,
            VertexFormat.DrawMode.QUADS,
            256,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ShaderProgram.POSITION_TEXTURE_COLOR_PROGRAM)
                    .texture(new RenderPhase.Texture(
                            Identifier.of("shoreline", "textures/snowflake.png"),
                            TriState.FALSE,
                            false))
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .build(false)
    );
}
