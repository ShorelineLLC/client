package net.shoreline.client.api.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.imixin.IPostEffectProcessor;
import net.shoreline.client.util.Globals;
import org.lwjgl.opengl.GL30C;

// Thanks ladysnake!
public class ShaderManager implements Globals
{
    private ShaderFramebuffer framebuffer;

    public ManagedShaderEffect filledShaderEffect;
    public ManagedShaderEffect gradientShaderEffect;
    public ManagedShaderEffect imageShaderEffect;

    public void reloadShaders()
    {
        if (framebuffer == null || filledShaderEffect == null || gradientShaderEffect == null)
        {
            reloadShadersInternal();
        }
    }

    public void reloadShadersInternal()
    {
        framebuffer = new ShaderFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
        filledShaderEffect = ShaderEffectManager.getInstance().manage(new Identifier("shoreline", "shaders/post/outline.json"));
        gradientShaderEffect = ShaderEffectManager.getInstance().manage(new Identifier("shoreline", "shaders/post/gradient.json"));
        imageShaderEffect = ShaderEffectManager.getInstance().manage(new Identifier("shoreline", "shaders/post/image.json"));
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
        framebuffer.beginWrite(true);
        // Render callbacks here
        runnable.run();
        framebuffer.endWrite();
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mcFramebuffer.fbo);
        mcFramebuffer.beginWrite(false);
        Framebuffer currentBuffer = mc.getFramebuffer();
        PostEffectProcessor effect = shaderEffect.getShaderEffect();
        if (effect != null)
        {
            ((IPostEffectProcessor) effect).overwriteBuffer("bufIn", framebuffer);
            Framebuffer bufOut = effect.getSecondaryTarget("bufOut");
            // Setup shader here
            setup.run();
            framebuffer.clear(false);
            currentBuffer.beginWrite(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
            RenderSystem.backupProjectionMatrix();
            bufOut.draw(bufOut.textureWidth, bufOut.textureHeight, false);
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
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
}
