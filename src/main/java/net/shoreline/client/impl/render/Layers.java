package net.shoreline.client.impl.render;

import net.minecraft.client.render.RenderLayer;

public class Layers
{
    public static final RenderLayer.MultiPhase QUADS = RenderLayer.of(
            "shoreline_quads", 156, false, true, Pipelines.QUADS,
            RenderLayer.MultiPhaseParameters.builder().build(false));

    public static final RenderLayer.MultiPhase DEBUG_LINES = RenderLayer.of(
            "shoreline_debug_lines", 1536, Pipelines.DEBUG_LINES,
            RenderLayer.MultiPhaseParameters.builder().build(false));
}
