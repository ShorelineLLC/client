package net.shoreline.client.impl.render.manager;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.imixin.*;
import net.shoreline.client.impl.module.render.ShadersModule;
import net.shoreline.client.impl.render.shader.ShaderEffect;
import net.shoreline.client.impl.render.shader.Uniform;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class ShaderManager extends GenericFeature
{
    private final OutlineVertexConsumerProvider vertexConsumerProvider;

    private final Framebuffer framebuffer;
    private final RenderPhase.Target target;

    private final Map<Identifier, RenderLayer> layerCache = new HashMap<>();

    private final ShadersModule shadersModule;

    public ShaderManager(ShadersModule shadersModule)
    {
        super("Shaders");
        this.shadersModule = shadersModule;
        this.vertexConsumerProvider = new OutlineVertexConsumerProvider(VertexConsumerProvider.immediate(new BufferAllocator(256)));
        this.framebuffer = new SimpleFramebuffer(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), true);
        this.framebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        this.target = new RenderPhase.Target("shader_target", () ->
        {
            if (!shadersModule.getDepth())
            {
                framebuffer.copyDepthFrom(mc.getFramebuffer());
            }

            framebuffer.beginWrite(false);

        }, () -> mc.getFramebuffer().beginWrite(false));
    }

    public void begin()
    {
        framebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        framebuffer.clear();

        mc.getFramebuffer().beginWrite(false);
    }

    public void render(ShaderEffect shaderEffect)
    {
        Identifier id = Identifier.of(ShorelineMod.MOD_ID, shaderEffect.getName());
        PostEffectProcessor shader = mc.getShaderLoader().loadPostEffect(id, DefaultFramebufferSet.MAIN_ONLY);

        ShaderProgram program = ((IPostEffectProcessor) shader).getPasses().getFirst().getProgram();
        program.addSamplerTexture("DiffuseSampler", framebuffer.getColorAttachment());

        for (Uniform<?> uniform : shaderEffect.getUniforms().values())
        {
            uniform.applyUniform(program);
        }

        shader.render(framebuffer, ((IGameRenderer) mc.gameRenderer).getPool());
        mc.getFramebuffer().beginWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);

        framebuffer.drawInternal(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    public void draw()
    {
        vertexConsumerProvider.draw();
    }

    public void clearCache()
    {
        layerCache.clear();
    }

    public void resize(int width, int height)
    {
        if (framebuffer != null)
        {
            framebuffer.resize(width, height);
        }
    }

    public VertexConsumerProvider createVertexConsumer(VertexConsumerProvider parent, Color color)
    {
        return layer ->
        {
            VertexConsumer parentBuffer = parent.getBuffer(layer);
            if (!(layer instanceof RenderLayer.MultiPhase) || ((IMultiPhaseParameters) (Object) ((IMultiPhase) layer).hookGetPhases()).getOutlineMode() == RenderLayer.OutlineMode.NONE)
            {
                return parentBuffer;
            }

            vertexConsumerProvider.setColor(color.getRed(), color.getGreen(), color.getBlue(), 255);

            RenderPhase.TextureBase texture = ((IMultiPhaseParameters) (Object) ((IMultiPhase) layer).hookGetPhases()).getTexture();
            RenderLayer outlineLayer = getOutlineLayer(texture);
            if (outlineLayer == null)
            {
                return parentBuffer;
            }

            VertexConsumer outlineBuffer = vertexConsumerProvider.getBuffer(outlineLayer);
            if (outlineBuffer == null)
            {
                return parentBuffer;
            }

            return VertexConsumers.union(outlineBuffer, parentBuffer);
        };
    }

    private RenderLayer getOutlineLayer(RenderPhase.TextureBase texture)
    {
        Optional<Identifier> id = ((ITextureBase) texture).hookGetId();
        return id.map(identifier -> layerCache.computeIfAbsent(identifier, layer ->
                RenderLayer.of(
                        "shoreline_overlay_" + id.get().getPath(),
                        VertexFormats.POSITION_TEXTURE_COLOR,
                        VertexFormat.DrawMode.QUADS,
                        1536,
                        RenderLayer.MultiPhaseParameters.builder()
                                .program(RenderPhase.OUTLINE_PROGRAM)
                                .texture(texture)
                                .cull(RenderPhase.DISABLE_CULLING)
                                .depthTest(shadersModule.getDepth() ? RenderPhase.ALWAYS_DEPTH_TEST : RenderPhase.LEQUAL_DEPTH_TEST)
                                .target(target)
                                .build(RenderLayer.OutlineMode.IS_OUTLINE)
                )
        )).orElse(null);
    }
}
