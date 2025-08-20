package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.render.RenderEntityWorldEvent;
import net.shoreline.client.impl.event.render.RenderShaderEvent;
import net.shoreline.client.impl.render.shader.ShaderEffect;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class ShadersModule extends Toggleable
{
    public ShadersModule()
    {
        super("Shaders", "Renders shaders over entities", GuiCategory.RENDER);
    }

    @EventListener
    public void onRenderShader(RenderShaderEvent event)
    {
        if (!checkNull())
        {
            Managers.SHADER.begin();
        }
    }

    @EventListener
    public void onRenderEntity(RenderEntityWorldEvent event)
    {
        event.cancel();
        event.setVertexConsumerProvider(Managers.SHADER.createVertexConsumer(event.getVertexConsumerProvider(), Color.WHITE));
    }

    @EventListener
    public void onRenderEntityPost(RenderEntityWorldEvent.Post event)
    {
        Managers.SHADER.getVertexConsumerProvider().draw();
        Managers.SHADER.render(new DefaultShaderEffect());
    }

    public static class DefaultShaderEffect extends ShaderEffect
    {
        public DefaultShaderEffect()
        {
            super("DefaultOutlineFill");
            addIntUniform("sobel", 1);
            addVec2Uniform("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
            addVec4Uniform("color", 1.0f, 1.0f, 1.0f, 0.5f);
            addIntUniform("samples", 8);
            addIntUniform("steps", 8);
            addIntUniform("dots", 0);
            addIntUniform("dotRadius", 8);
            addIntUniform("fastOutline", 1);
            addFltUniform("radius", 1.0f);
            addIntUniform("glow", 0);
            addFltUniform("glowRadius", 1.0f);
        }
    }
}
