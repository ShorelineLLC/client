package net.shoreline.client.impl.module.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.render.RenderEntityWorldEvent;
import net.shoreline.client.impl.event.render.RenderShaderEvent;
import net.shoreline.client.impl.event.render.item.RenderHandEvent;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.render.shader.ShaderEffect;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class ShadersModule extends Toggleable
{
    public static ShadersModule INSTANCE;

    Config<Boolean> handsConfig = new BooleanConfig.Builder("Hands")
            .setDescription("Render shaders over hands")
            .setDefaultValue(true).build();
    Config<Boolean> playersConfig = new BooleanConfig.Builder("Players")
            .setDescription("Render shaders over other players")
            .setDefaultValue(true).build();
    Config<Boolean> selfConfig = new BooleanConfig.Builder("Self")
            .setDescription("Render shaders over the player")
            .setVisible(() -> playersConfig.getValue())
            .setDefaultValue(true).build();
    Config<Boolean> crystalsConfig = new BooleanConfig.Builder("Crystals")
            .setDescription("Render shaders over crystals")
            .setDefaultValue(true).build();
    Config<Boolean> itemsConfig = new BooleanConfig.Builder("Items")
            .setDescription("Render shaders over items")
            .setDefaultValue(true).build();
    Config<Boolean> thrownConfig = new BooleanConfig.Builder("Thrown")
            .setDescription("Render shaders over thrown items")
            .setDefaultValue(true).build();
    Config<Boolean> passiveConfig = new BooleanConfig.Builder("Passive")
            .setDescription("Render shaders over hands")
            .setDefaultValue(true).build();
    Config<Boolean> hostilesConfig = new BooleanConfig.Builder("Hostiles")
            .setDescription("Render shaders over hands")
            .setDefaultValue(true).build();
    public Config<Void> renderConfig = new ConfigGroup.Builder("Target")
            .addAll(handsConfig, playersConfig, selfConfig, crystalsConfig, itemsConfig,
                    thrownConfig, passiveConfig, hostilesConfig).build();

    Config<Float> opacity = new NumberConfig.Builder<Float>("Opacity")
            .setMin(0.0f).setDefaultValue(0.5f).setMax(1.0f)
            .setDescription("Opacity for the shader fill").build();
    Config<Boolean> depthConfig = new BooleanConfig.Builder("ThroughWalls")
            .setDescription("Renders shaders through walls")
            .setDefaultValue(true).build();

    public ShadersModule()
    {
        super("Shaders", "Renders shaders over entities", GuiCategory.RENDER);
        INSTANCE = this;

        depthConfig.addListener(v ->
        {
            if (Managers.SHADER != null)
            {
                Managers.SHADER.clearCache();
            }
        });
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
        if (!shouldRenderShader(event.getEntity()))
        {
            return;
        }

        boolean isFriend = Managers.SOCIAL.isFriend(event.getEntity());
        event.cancel();
        event.setVertexConsumerProvider(Managers.SHADER.createVertexConsumer(
                event.getVertexConsumerProvider(),
                isFriend ? SocialsModule.INSTANCE.getFriendsColor() : ThemeModule.INSTANCE.getPrimaryColor()));
    }

    @EventListener
    public void onRenderEntityPost(RenderEntityWorldEvent.Post event)
    {
        Managers.SHADER.draw();
    }

    @EventListener
    public void onRenderHand(RenderHandEvent event)
    {
        if (handsConfig.getValue())
        {
            event.setVertexConsumerProvider(Managers.SHADER.createVertexConsumer(
                    event.getVertexConsumerProvider(), ThemeModule.INSTANCE.getPrimaryColor()));
        }
    }

    @EventListener
    public void onRenderHandPost(RenderHandEvent.Post event)
    {
        if (handsConfig.getValue())
        {
            Managers.SHADER.draw();
        }
    }

    public boolean getDepth()
    {
        return !depthConfig.getValue();
    }

    private boolean shouldRenderShader(Entity entity)
    {
        if (entity instanceof PlayerEntity && playersConfig.getValue())
        {
            return entity != mc.player || selfConfig.getValue();
        }

        return EntityUtil.isHostile(entity) && hostilesConfig.getValue()
                || EntityUtil.isPassive(entity) && passiveConfig.getValue()
                || entity instanceof ItemEntity && itemsConfig.getValue()
                || entity instanceof EndCrystalEntity && crystalsConfig.getValue()
                || (entity instanceof ExperienceBottleEntity || entity instanceof EnderPearlEntity) && thrownConfig.getValue();
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
            addIntUniform("fastOutline", 0);
            addFltUniform("radius", 1.0f);
            addIntUniform("glow", 0);
            addFltUniform("glowRadius", 1.0f);
        }
    }
}
