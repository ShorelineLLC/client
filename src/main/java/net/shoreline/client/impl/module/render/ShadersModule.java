package net.shoreline.client.impl.module.render;

import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.impl.ResettableManagedShaderEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.EntityOutlineEvent;
import net.shoreline.client.impl.event.gui.screen.pack.RefreshPacksEvent;
import net.shoreline.client.impl.event.render.ReloadShaderEvent;
import net.shoreline.client.impl.event.render.RenderShaderEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorGameRenderer;
import net.shoreline.client.util.world.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.sql.Ref;

/**
 * @author linus
 * @since 1.0
 */
public class ShadersModule extends ToggleModule {

    Config<ShaderMode> modeConfig = register(new EnumConfig<>("Mode", "The shader mode", ShaderMode.NORMAL, ShaderMode.values()));
    Config<Boolean> outlineConfig = register(new BooleanConfig("Outline", "Adds an outline around the shader", true));
    Config<Float> lineWidthConfig = register(new NumberConfig<>("Width", "The outline width", 0.5f, 1.0f, 2.0f, () -> outlineConfig.getValue()));
    Config<Boolean> dotsConfig = register(new BooleanConfig("Dots", "Hacker esp", false));
    Config<Integer> dotRadiusConfig = register(new NumberConfig<>("DotRadius", "Width between the dots", 5, 8, 16, () -> dotsConfig.getValue()));
    Config<Boolean> handsConfig = register(new BooleanConfig("Hands", "Render shaders on first-person hands", true));
    Config<Boolean> selfConfig = register(new BooleanConfig("Self", "Render shaders on the player", true));
    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Render shaders on other players", true));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Render shaders on monsters", true));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Render shaders on animals", true));
    Config<Boolean> itemsConfig = register(new BooleanConfig("Items", "Render shaders on items", true));
    Config<Boolean> otherConfig = register(new BooleanConfig("Others", "Render shaders on crystals", true));
    Config<Boolean> invisiblesConfig = register(new BooleanConfig("Invisibles", "Render shaders on invisible entities", true));
    Config<Color> colorConfig = register(new ColorConfig("Color", "The color of the shader", new Color(1.0f, 0.0f, 0.0f, 0.2f)));

    public ShadersModule()
    {
        super("Shaders", "Renders shaders over entities", ModuleCategory.RENDER);
    }

    @EventListener
    public void onEntityOutline(EntityOutlineEvent event)
    {
        if (checkShaders(event.getEntity()))
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderShader(RenderShaderEvent event)
    {
        event.cancel();
        // TODO: Add more modes
        ManagedShaderEffect shaderEffect = Managers.SHADER.getFilledShaderEffect();
        shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
        shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
        shaderEffect.setUniformValue("dotRadius", dotsConfig.getValue() ? dotRadiusConfig.getValue() : 0);
        shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, colorConfig.getValue().getAlpha() / 255.0f);
        shaderEffect.render(mc.getTickDelta());
    }

    @EventListener
    public void onPackRefresh(RefreshPacksEvent event)
    {
        Managers.SHADER.reloadShaders();
    }

    @EventListener
    public void onReloadShader(ReloadShaderEvent event)
    {
        if (handsConfig.getValue())
        {
            ManagedShaderEffect shaderEffect = Managers.SHADER.getFilledShaderEffect1();
            if (shaderEffect == null)
            {
                return;
            }
            Managers.SHADER.applyShader(shaderEffect, () ->
            {
                shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
                shaderEffect.setUniformValue("dotRadius", dotsConfig.getValue() ? dotRadiusConfig.getValue() : 0);
                shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, colorConfig.getValue().getAlpha() / 255.0f);
                shaderEffect.render(mc.getTickDelta());
            }, () ->
            {
                ((AccessorGameRenderer) mc.gameRenderer).hookRenderHand(event.getMatrixStack(), mc.gameRenderer.getCamera(), event.getDelta());
            });
        }
    }

    private boolean checkShaders(Entity entity)
    {
        if (entity instanceof PlayerEntity && playersConfig.getValue())
        {
            return selfConfig.getValue() || entity != mc.player;
        }
        return (!entity.isInvisible() || invisiblesConfig.getValue())
                && (EntityUtil.isMonster(entity) && monstersConfig.getValue()
                || (EntityUtil.isNeutral(entity)
                || EntityUtil.isPassive(entity)) && animalsConfig.getValue())
                || entity instanceof EndCrystalEntity && otherConfig.getValue()
                || entity instanceof ItemEntity && itemsConfig.getValue();
    }

    private enum ShaderMode
    {
        NORMAL,
        GRADIENT
    }
}
