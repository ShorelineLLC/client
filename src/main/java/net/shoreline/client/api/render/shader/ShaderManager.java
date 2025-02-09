package net.shoreline.client.api.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.render.satin.ManagedShaderEffect;
import net.shoreline.client.api.render.satin.ShaderEffectManager;
import net.shoreline.client.impl.imixin.IPostEffectProcessor;
import net.shoreline.client.mixin.accessor.AccessorMultiPhase;
import net.shoreline.client.mixin.accessor.AccessorMultiPhaseParameters;
import net.shoreline.client.mixin.accessor.AccessorTextureBase;
import net.shoreline.client.util.Globals;
import org.lwjgl.opengl.GL30C;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Thanks ladysnake!
public class ShaderManager implements Globals
{
    private final OutlineVertexConsumerProvider vertexConsumerProvider = new OutlineVertexConsumerProvider(VertexConsumerProvider.immediate(new BufferAllocator(256)));
    private final Function<RenderPhase.TextureBase, RenderLayer> layerCreator;
    private final RenderPhase.Target target;

    private ShaderFramebuffer framebuffer;

    public ManagedShaderEffect filledShaderEffect;
    public ManagedShaderEffect gradientShaderEffect;
    public ManagedShaderEffect imageShaderEffect;
    public ManagedShaderEffect glowingShaderEffect;
    public ManagedShaderEffect rainbowShaderEffect;

    public ShaderManager()
    {
        target = new RenderPhase.Target("shader_target", () -> {}, () -> {});
        layerCreator = memoizeTexture(texture -> RenderLayer.of("shoreline_overlay", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 1536, RenderLayer.MultiPhaseParameters.builder()
                .program(RenderPhase.OUTLINE_PROGRAM).cull(RenderPhase.DISABLE_CULLING).texture(texture).depthTest(RenderPhase.ALWAYS_DEPTH_TEST).target(target).build(RenderLayer.OutlineMode.IS_OUTLINE)));
    }

    public void reloadShaders()
    {
        if (framebuffer == null || filledShaderEffect == null || gradientShaderEffect == null || imageShaderEffect == null || glowingShaderEffect == null)
        {
            reloadShadersInternal();
        }
    }

    public void reloadShadersInternal()
    {
        framebuffer = new ShaderFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
        filledShaderEffect = ShaderEffectManager.getInstance().manage(Identifier.of("shoreline", "shaders/post/outline.json"));
        gradientShaderEffect = ShaderEffectManager.getInstance().manage(Identifier.of("shoreline", "shaders/post/gradient.json"));
        imageShaderEffect = ShaderEffectManager.getInstance().manage(Identifier.of("shoreline", "shaders/post/image.json"));
        glowingShaderEffect = ShaderEffectManager.getInstance().manage(Identifier.of("shoreline", "shaders/post/glowing.json"));
        rainbowShaderEffect = ShaderEffectManager.getInstance().manage(Identifier.of("shoreline", "shaders/post/rainbow.json"));
    }

    public void applyShader(ManagedShaderEffect shaderEffect, Runnable setup, Runnable runnable)
    {
        Framebuffer mcFramebuffer = mc.getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        if (framebuffer.textureWidth != mcFramebuffer.textureWidth || framebuffer.textureHeight != mcFramebuffer.textureHeight)
        {
            framebuffer.resize(mcFramebuffer.textureWidth, mcFramebuffer.textureHeight, false);
        }

        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer.fbo);
        framebuffer.beginWrite(false);
        runnable.run();
        // Render callbacks here
        framebuffer.endWrite();
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mcFramebuffer.fbo);
        mcFramebuffer.beginWrite(false);
        PostEffectProcessor effect = shaderEffect.getShaderEffect();
        if (effect != null)
        {
            ((IPostEffectProcessor) effect).overwriteBuffer("bufIn", framebuffer);
            Framebuffer bufOut = effect.getSecondaryTarget("bufOut");
            // Setup shader here
            setup.run();
            framebuffer.clear(false);
            mcFramebuffer.beginWrite(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
            RenderSystem.backupProjectionMatrix();
            bufOut.draw(bufOut.textureWidth, bufOut.textureHeight, false);
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }

    }

    public VertexConsumerProvider createVertexConsumers(VertexConsumerProvider parent, Color color)
    {
        return layer ->
        {
            VertexConsumer parentBuffer = parent.getBuffer(layer);
            if (!(layer instanceof RenderLayer.MultiPhase) || ((AccessorMultiPhaseParameters) (Object) ((AccessorMultiPhase) layer).hookGetPhases()).hookGetOutlineMode() == RenderLayer.OutlineMode.NONE)
            {
                return parentBuffer;
            }

            vertexConsumerProvider.setColor(color.getRed(), color.getGreen(), color.getBlue(), 255);

            VertexConsumer outlineBuffer = vertexConsumerProvider.getBuffer(layerCreator.apply(((AccessorMultiPhaseParameters) (Object) ((AccessorMultiPhase) layer).hookGetPhases()).hookGetTexture()));
            return outlineBuffer != null ? outlineBuffer : parentBuffer;
        };
    }

    private Function<RenderPhase.TextureBase, RenderLayer> memoizeTexture(Function<RenderPhase.TextureBase, RenderLayer> function)
    {
        return new Function<>()
        {
            private final Map<Identifier, RenderLayer> cache = new HashMap<>();

            public RenderLayer apply(RenderPhase.TextureBase texture)
            {
                return this.cache.computeIfAbsent(((AccessorTextureBase) texture).hookGetId().get(), id -> function.apply(texture));
            }
        };
    }

    // Instance without overwritten buffers
    public ManagedShaderEffect getFilledShaderEffect()
    {
        return filledShaderEffect;
    }

    public ManagedShaderEffect getGradientShaderEffect()
    {
        return gradientShaderEffect;
    }

    public ManagedShaderEffect getImageShaderEffect()
    {
        return imageShaderEffect;
    }

    public ManagedShaderEffect getGlowingShaderEffect()
    {
        return glowingShaderEffect;
    }

    public ManagedShaderEffect getRainbowShaderEffect()
    {
        return rainbowShaderEffect;
    }
}
