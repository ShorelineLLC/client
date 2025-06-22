package net.shoreline.client.impl.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;

public class RenderLayers
{
    public static final RenderPipeline PIPELINE_TEXT_CUSTOM = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET, RenderPipelines.FOG_SNIPPET)
                    .withLocation(Identifier.of("shoreline", "pipeline/text_lumi"))
                    .withVertexShader(Identifier.of("shoreline", "core/custom_text"))
                    .withFragmentShader(Identifier.of("shoreline", "core/custom_text"))
                    .withSampler("Sampler0")
                    .withSampler("Sampler2")
                    .withDepthBias(-1.0f, -10.0f)
                    .build()
    );
}
