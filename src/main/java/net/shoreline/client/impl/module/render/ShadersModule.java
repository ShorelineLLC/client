package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.render.RenderEntityWorldEvent;
import net.shoreline.client.impl.event.render.RenderShaderEvent;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.render.Theme;
import net.shoreline.client.impl.render.shader.ShaderEffect;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class ShadersModule extends Toggleable
{
    Config<Float> opacity = new NumberConfig.Builder<Float>("Opacity")
            .setMin(0.0f).setDefaultValue(0.5f).setMax(1.0f)
            .setDescription("Opacity for the shader fill").build();
    Config<Boolean> fastOutline = new BooleanConfig.Builder("FastOutline")
            .setDefaultValue(false).build();

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
    public void onRenderShader(RenderShaderEvent.Post event)
    {
        if (!checkNull())
        {
            Managers.SHADER.render(new DefaultShaderEffect());
        }
    }

    @EventListener
    public void onRenderEntity(RenderEntityWorldEvent event)
    {
        event.cancel();
        event.setVertexConsumerProvider(Managers.SHADER.createVertexConsumer(event.getVertexConsumerProvider(), ThemeModule.INSTANCE.getPrimaryColor()));
    }

    @EventListener
    public void onRenderEntityPost(RenderEntityWorldEvent.Post event)
    {
        Managers.SHADER.getVertexConsumerProvider().draw();
    }

    public class DefaultShaderEffect extends ShaderEffect
    {
        public DefaultShaderEffect()
        {
            super("DefaultOutlineFill");
            Color color = ThemeModule.INSTANCE.getPrimaryColor();
            addIntUniform("sobel", 1);
            addVec2Uniform("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
            addVec4Uniform("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, opacity.getValue());
            addIntUniform("samples", 8);
            addIntUniform("steps", 8);
            addIntUniform("dots", 0);
            addIntUniform("dotRadius", 8);
            addIntUniform("fastOutline", fastOutline.getValue() ? 1 : 0);
            addFltUniform("radius", 1.0f);
            addIntUniform("glow", 0);
            addFltUniform("glowRadius", 1.0f);
        }
    }
}
