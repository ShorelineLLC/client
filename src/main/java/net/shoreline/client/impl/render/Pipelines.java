package net.shoreline.client.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class Pipelines
{
    public static final RenderPipeline QUADS = RenderPipelines
            .register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withLocation("pipeline/shoreline_quads")
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthWrite(false)
                    .withCull(false)
                    .build());

    public static final RenderPipeline DEBUG_LINES = RenderPipelines
            .register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withVertexShader("core/position_color")
                    .withLocation("pipeline/shoreline_debug_lines")
                    .withFragmentShader("core/position_color")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthWrite(false)
                    .withCull(false)
                    .build());

    public static final RenderPipeline TEXT_CUSTOM = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET, RenderPipelines.FOG_SNIPPET)
                    .withLocation(Identifier.of("shoreline", "pipeline/text_lumi"))
                    .withVertexShader(Identifier.of("shoreline", "core/text"))
                    .withFragmentShader(Identifier.of("shoreline", "core/text"))
                    .withSampler("Sampler0")
                    .withSampler("Sampler2")
                    .withDepthBias(-1.0f, -10.0f)
                    .build()
    );
}
