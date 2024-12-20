package net.shoreline.client.impl.module.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.satin.ManagedShaderEffect;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.event.render.RenderShaderEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.render.entity.RenderCrystalEvent;
import net.shoreline.client.impl.event.render.entity.RenderEntityEvent;
import net.shoreline.client.impl.event.render.entity.RenderItemEvent;
import net.shoreline.client.impl.event.render.entity.RenderLabelEvent;
import net.shoreline.client.impl.event.render.item.RenderFirstPersonEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorGameRenderer;
import net.shoreline.client.mixin.accessor.AccessorWorldRenderer;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.world.BlockUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;
import java.util.UUID;

/**
 * @author linus
 * @since 1.0
 */
public class ShadersModule extends ToggleModule
{
    private static ShadersModule INSTANCE;

    Config<Float> rangeConfig = register(new NumberConfig<>("RenderDistance", "The shader render range", 10.0f, 50.0f, 200.0f));
    Config<Boolean> textureConfig = register(new BooleanConfig("Texture", "Renders the entity model texture", true));
    Config<Boolean> outlineConfig = register(new BooleanConfig("Outline", "Adds an outline around the shader", true));
    Config<Float> lineWidthConfig = register(new NumberConfig<>("Width", "The outline width", 1.0f, 1.5f, 10.0f, () -> outlineConfig.getValue()));
    Config<Boolean> fastOutlineConfig = register(new BooleanConfig("FastOutline", "Faster outline calculations", false, () -> outlineConfig.getValue()));
    Config<Integer> qualityConfig = register(new NumberConfig<>("Quality", "The outline pixel quality", 2, 10, 32, () -> fastOutlineConfig.getValue() && outlineConfig.getValue()));
    Config<Integer> stepsConfig = register(new NumberConfig<>("Steps", "The number of steps for finding edges", 2, 4, 32, () -> fastOutlineConfig.getValue() && outlineConfig.getValue()));
    Config<Boolean> glowConfig = register(new BooleanConfig("Glow", "Glow outline", false, () -> outlineConfig.getValue()));
    Config<Float> glowRadiusConfig = register(new NumberConfig<>("GlowFactor", "The glow radius", 0.1f, 1.0f, 3.0f, () -> glowConfig.getValue() && outlineConfig.getValue()));
    Config<ShaderMode> modeConfig = register(new EnumConfig<>("Fill", "The shader mode", ShaderMode.OFF, ShaderMode.values()));
    Config<Float> factorConfig = register(new NumberConfig<>("Factor", "The gradient factor", 0.1f, 8.0f, 10.0f, () -> modeConfig.getValue() == ShaderMode.GRADIENT));
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The gradient speed factor", 0.01f, 1.5f, 10.0f, () -> modeConfig.getValue() == ShaderMode.GRADIENT));
    Config<Float> marbleFactorConfig = register(new NumberConfig<>("Flow", "The marble speed factor", 0.001f, 0.003f, 0.01f, () -> modeConfig.getValue() == ShaderMode.MARBLE));
    Config<Boolean> dotsConfig = register(new BooleanConfig("Dots", "Hacker esp", false, () -> modeConfig.getValue() == ShaderMode.DEFAULT));
    Config<Integer> dotRadiusConfig = register(new NumberConfig<>("DotRadius", "Width between the dots", 5, 8, 16, () -> dotsConfig.getValue() && modeConfig.getValue() == ShaderMode.DEFAULT));
    Config<Boolean> handsConfig = register(new BooleanConfig("Hands", "Render shaders on first-person hands", true));
    Config<Boolean> selfConfig = register(new BooleanConfig("Self", "Render shaders on the player", true));
    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Render shaders on other players", true));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Render shaders on monsters", true));
    Config<Boolean> neutralsConfig = register(new BooleanConfig("Neutrals", "Render shaders on neutrals", true));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Render shaders on animals", true));
    Config<Boolean> itemsConfig = register(new BooleanConfig("Items", "Render shaders on items", true));
    Config<Boolean> otherConfig = register(new BooleanConfig("Crystals", "Render shaders on crystals", true));
    Config<Boolean> projectilesConfig = register(new BooleanConfig("Projectiles", "Render shaders on projectiles", true));
    Config<Boolean> invisiblesConfig = register(new BooleanConfig("Invisibles", "Render shaders on invisible entities", true));
    Config<Boolean> chestsConfig = register(new BooleanConfig("Chests", "Render players through walls", false));
    Config<Boolean> echestsConfig = register(new BooleanConfig("EnderChests", "Render players through walls", false));
    Config<Boolean> shulkersConfig = register(new BooleanConfig("Shulkers", "Render players through walls", false));
    Config<Color> colorConfig = register(new ColorConfig("Color", "The color of the shader", new Color(1.0f, 0.0f, 0.0f, 0.4f)));
    Config<Color> gradientConfig = register(new ColorConfig("GradientColor", "The gradient color of the shader", new Color(1.0f, 1.0f, 1.0f), true, false, () -> modeConfig.getValue() == ShaderMode.GRADIENT));

    private float shaderTime;

    private int textureId;
    private boolean ignoreEntityRender;

    public ShadersModule()
    {
        super("Shaders", "Renders shaders over entities", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    public static ShadersModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onEnable()
    {
        if (modeConfig.getValue() == ShaderMode.IMAGE)
        {
            loadShaderImage();
        }
        ignoreEntityRender = false;
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (mc.player == null)
        {
            return;
        }
        if (event.getConfig() == modeConfig && modeConfig.getValue() == ShaderMode.IMAGE
                && event.getStage() == StageEvent.EventStage.POST)
        {
            loadShaderImage();
        }
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event)
    {
        if (modeConfig.getValue() == ShaderMode.IMAGE)
        {
            loadShaderImage();
        }
    }

    @EventListener
    public void onRenderEntityWorld(RenderShaderEvent event)
    {
        VertexConsumerProvider vertexConsumerProvider = ((AccessorWorldRenderer) mc.worldRenderer).hookGetBufferBuilders().getEntityVertexConsumers();

        switch (modeConfig.getValue())
        {
            case DEFAULT, OFF ->
            {
                final ManagedShaderEffect shaderEffect = Managers.SHADER.getFilledShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                Managers.SHADER.applyShader(shaderEffect, () ->
                {
                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, modeConfig.getValue() == ShaderMode.DEFAULT ? colorConfig.getValue().getAlpha() / 255.0f : 0.0f);
                    shaderEffect.setUniformValue("samples", qualityConfig.getValue());
                    shaderEffect.setUniformValue("steps", stepsConfig.getValue());
                    shaderEffect.setUniformValue("dots", dotsConfig.getValue() && modeConfig.getValue() != ShaderMode.OFF ? 1 : 0);
                    shaderEffect.setUniformValue("dotRadius", dotRadiusConfig.getValue());
                    shaderEffect.setUniformValue("fastOutline", fastOutlineConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
                    shaderEffect.setUniformValue("glow", glowConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("glowRadius", glowRadiusConfig.getValue());
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                }, () ->
                {
                    renderEntities(event.getTickDelta(), event.getMatrices(), vertexConsumerProvider);
                });
            }
            case GRADIENT ->
            {
                final ManagedShaderEffect shaderEffect = Managers.SHADER.getGradientShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                Managers.SHADER.applyShader(shaderEffect, () ->
                {
                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, colorConfig.getValue().getAlpha() / 255.0f);
                    shaderEffect.setUniformValue("samples", qualityConfig.getValue());
                    shaderEffect.setUniformValue("steps", stepsConfig.getValue());
                    shaderEffect.setUniformValue("color1", gradientConfig.getValue().getRed() / 255.0f, gradientConfig.getValue().getGreen() / 255.0f, gradientConfig.getValue().getBlue() / 255.0f, gradientConfig.getValue().getAlpha() / 255.0f);
                    shaderEffect.setUniformValue("factor", factorConfig.getValue() * 10.0f);
                    shaderEffect.setUniformValue("time", shaderTime);
                    shaderEffect.setUniformValue("fastOutline", fastOutlineConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
                    shaderEffect.setUniformValue("glow", glowConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("glowRadius", glowRadiusConfig.getValue());
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                    shaderTime += speedConfig.getValue();
                }, () ->
                {
                    renderEntities(event.getTickDelta(), event.getMatrices(), vertexConsumerProvider);
                });
            }
            case MARBLE ->
            {
                final ManagedShaderEffect shaderEffect = Managers.SHADER.getGlowingShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                Managers.SHADER.applyShader(shaderEffect, () ->
                {
                    shaderEffect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("samples", qualityConfig.getValue());
                    shaderEffect.setUniformValue("steps", stepsConfig.getValue());
                    shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, colorConfig.getValue().getAlpha() / 255.0f);
                    shaderEffect.setUniformValue("time", shaderTime);
                    shaderEffect.setUniformValue("fastOutline", fastOutlineConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
                    shaderEffect.setUniformValue("glow", glowConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("glowRadius", glowRadiusConfig.getValue());
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                    shaderTime += marbleFactorConfig.getValue();
                }, () ->
                {
                    renderEntities(event.getTickDelta(), event.getMatrices(), vertexConsumerProvider);
                });
            }
//            case FLAME ->
//            {
//                final ManagedShaderEffect shaderEffect = Managers.SHADER.getFlameShaderEffect();
//                if (shaderEffect == null)
//                {
//                    return;
//                }
//                Managers.SHADER.applyShader(shaderEffect, () ->
//                {
//                    shaderEffect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
//                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
//                    shaderEffect.setUniformValue("samples", qualityConfig.getValue());
//                    shaderEffect.setUniformValue("steps", stepsConfig.getValue());
//                    shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, colorConfig.getValue().getAlpha() / 255.0f);
//                    shaderEffect.setUniformValue("time", shaderTime);
//                    shaderEffect.setUniformValue("fastOutline", fastOutlineConfig.getValue() ? 1 : 0);
//                    shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
//                    shaderEffect.setUniformValue("glow", glowConfig.getValue() ? 1 : 0);
//                    shaderEffect.setUniformValue("glowRadius", glowRadiusConfig.getValue());
//                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
//                    shaderTime += 0.005f;
//                }, () ->
//                {
//                    renderEntities(event.getTickDelta(), event.getMatrices(), vertexConsumerProvider);
//                });
//            }
            case IMAGE ->
            {
                final ManagedShaderEffect shaderEffect = Managers.SHADER.getImageShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                Managers.SHADER.applyShader(shaderEffect, () ->
                {
                    GlStateManager._activeTexture(GL32C.GL_TEXTURE0 + 1);
                    GlStateManager._bindTexture(textureId);
                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("samples", qualityConfig.getValue());
                    shaderEffect.setUniformValue("steps", stepsConfig.getValue());
                    shaderEffect.setUniformValue("imageTexture", 1);
                    shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, colorConfig.getValue().getAlpha() / 255.0f);
                    shaderEffect.setUniformValue("fastOutline", fastOutlineConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
                    shaderEffect.setUniformValue("glow", glowConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("glowRadius", glowRadiusConfig.getValue());
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                }, () ->
                {
                    renderEntities(event.getTickDelta(), event.getMatrices(), vertexConsumerProvider);
                });
            }
        }
    }

    @EventListener
    public void onRenderLabel(RenderLabelEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity && ignoreEntityRender)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderCrystal(RenderCrystalEvent event)
    {
        if (mc.player != null && !textureConfig.getValue() && otherConfig.getValue() && !ignoreEntityRender &&
                mc.player.squaredDistanceTo(event.endCrystalEntity) <= ((NumberConfig) rangeConfig).getValueSq())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderEntity(RenderEntityEvent event)
    {
        if (mc.player == null || textureConfig.getValue() || !checkShaders(event.entity) || ignoreEntityRender
                || mc.player.squaredDistanceTo(event.entity) > ((NumberConfig) rangeConfig).getValueSq())
        {
            return;
        }
        event.cancel();
    }

    @EventListener
    public void onRenderItem(RenderItemEvent event)
    {
        if (mc.player != null && !textureConfig.getValue() && itemsConfig.getValue() && !ignoreEntityRender &&
                mc.player.squaredDistanceTo(event.getItem()) <= ((NumberConfig) rangeConfig).getValueSq())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderArm(RenderFirstPersonEvent.Head event)
    {
        if (mc.player == null || textureConfig.getValue() || !handsConfig.getValue() || ignoreEntityRender)
        {
            return;
        }
        event.cancel();
    }
    
    private void renderEntities(float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider)
    {
        ignoreEntityRender = true;
        for (Entity entity : mc.world.getEntities())
        {
            if (checkShaders(entity))
            {
                if (mc.player.squaredDistanceTo(entity) > ((NumberConfig) rangeConfig).getValueSq())
                {
                    continue;
                }
                Vec3d camera = mc.gameRenderer.getCamera().getPos();
                double d = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
                double e = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
                double f = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());
                float g = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
                EntityRenderer<Entity> entityRenderer = (EntityRenderer<Entity>) mc.getEntityRenderDispatcher().getRenderer(entity);
                int light = mc.getEntityRenderDispatcher().getLight(entity, tickDelta);
                try
                {
                    Vec3d vec3d = entityRenderer.getPositionOffset(entity, tickDelta);
                    double x = (d - camera.x) + vec3d.x;
                    double y = (e - camera.y) + vec3d.y;
                    double z = (f - camera.z) + vec3d.z;
                    matrixStack.push();
                    if (ChamsModule.getInstance().isEnabled() && (entity instanceof LivingEntity entity1
                            && ChamsModule.getInstance().checkChams(entity1) || entity instanceof EndCrystalEntity && ChamsModule.getInstance().crystalsConfig.getValue()))
                    {
                        ChamsModule.getInstance().renderEntityChams(matrixStack, entity, tickDelta);
                    }
                    else
                    {
                        matrixStack.translate(x, y, z);
                        entityRenderer.render(entity, g, tickDelta, matrixStack, vertexConsumerProvider, light);
                        matrixStack.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ());
                    }
                    matrixStack.pop();
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                    Shoreline.error("Failed to render shader on entity!");
                }
            }
            RenderBuffers.postRender();

            // Blockentity shaders
            if (echestsConfig.getValue() || chestsConfig.getValue() || shulkersConfig.getValue())
            {
                for (BlockEntity blockEntity : BlockUtil.blockEntities())
                {
                    if (checkStorageShaders(blockEntity))
                    {
                        Vec3d vec3d = mc.gameRenderer.getCamera().getPos();
                        double d = vec3d.getX();
                        double e = vec3d.getY();
                        double g = vec3d.getZ();
                        BlockPos blockPos3 = blockEntity.getPos();
                        matrixStack.push();
                        matrixStack.translate((double)blockPos3.getX() - d, (double)blockPos3.getY() - e, (double)blockPos3.getZ() - g);
                        mc.getBlockEntityRenderDispatcher().render(blockEntity, tickDelta, matrixStack, vertexConsumerProvider);
                        matrixStack.pop();
                    }
                }
            }
        }

        // ciaohack solutions
        OtherClientPlayerEntity fakePlayerEntity = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("041f2043-a047-482e-a5b2-41c711badc42"), "nigger"));
        fakePlayerEntity.setPosition(0.0, -100000000.0, 0.0);
        fakePlayerEntity.setId(Integer.MAX_VALUE);
        EntityRenderer<Entity> entityRenderer = (EntityRenderer<Entity>) mc.getEntityRenderDispatcher().getRenderer(fakePlayerEntity);
        matrixStack.push();
        matrixStack.translate(0.0, -100000000.0, 0.0);
        entityRenderer.render(fakePlayerEntity, fakePlayerEntity.getYaw(), tickDelta, matrixStack, vertexConsumerProvider, 0);
        matrixStack.pop();
        ignoreEntityRender = false;
    }

    @EventListener
    public void onReloadShader(RenderWorldEvent.Hand event)
    {
        if (!handsConfig.getValue())
        {
            return;
        }
        switch (modeConfig.getValue())
        {
            case DEFAULT, OFF ->
            {
                final ManagedShaderEffect shaderEffect = Managers.SHADER.getFilledShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                Managers.SHADER.applyShader(shaderEffect, () ->
                {
                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, modeConfig.getValue() == ShaderMode.DEFAULT ? colorConfig.getValue().getAlpha() / 255.0f : 0.0f);
                    shaderEffect.setUniformValue("samples", qualityConfig.getValue());
                    shaderEffect.setUniformValue("steps", stepsConfig.getValue());
                    shaderEffect.setUniformValue("dots", dotsConfig.getValue() && modeConfig.getValue() != ShaderMode.OFF ? 1 : 0);
                    shaderEffect.setUniformValue("dotRadius", dotRadiusConfig.getValue());
                    shaderEffect.setUniformValue("fastOutline", fastOutlineConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
                    shaderEffect.setUniformValue("glow", glowConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("glowRadius", glowRadiusConfig.getValue());
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                }, () ->
                {
                    ignoreEntityRender = true;
                    ((AccessorGameRenderer) mc.gameRenderer).hookRenderHand(mc.gameRenderer.getCamera(), event.getTickDelta(), event.getMatrices().peek().getPositionMatrix());
                    ignoreEntityRender = false;
                });
            }
            case GRADIENT ->
            {
                final ManagedShaderEffect shaderEffect = Managers.SHADER.getGradientShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                Managers.SHADER.applyShader(shaderEffect, () ->
                {
                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, colorConfig.getValue().getAlpha() / 255.0f);
                    shaderEffect.setUniformValue("samples", qualityConfig.getValue());
                    shaderEffect.setUniformValue("steps", stepsConfig.getValue());
                    shaderEffect.setUniformValue("color1", gradientConfig.getValue().getRed() / 255.0f, gradientConfig.getValue().getGreen() / 255.0f, gradientConfig.getValue().getBlue() / 255.0f, gradientConfig.getValue().getAlpha() / 255.0f);
                    shaderEffect.setUniformValue("factor", factorConfig.getValue() * 10.0f);
                    shaderEffect.setUniformValue("time", shaderTime);
                    shaderEffect.setUniformValue("fastOutline", fastOutlineConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
                    shaderEffect.setUniformValue("glow", glowConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("glowRadius", glowRadiusConfig.getValue());
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                    shaderTime += speedConfig.getValue();
                }, () ->
                {
                    ignoreEntityRender = true;
                    ((AccessorGameRenderer) mc.gameRenderer).hookRenderHand(mc.gameRenderer.getCamera(), event.getTickDelta(), event.getMatrices().peek().getPositionMatrix());
                    ignoreEntityRender = false;
                });
            }
            case MARBLE ->
            {
                final ManagedShaderEffect shaderEffect = Managers.SHADER.getGlowingShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                Managers.SHADER.applyShader(shaderEffect, () ->
                {
                    shaderEffect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("samples", qualityConfig.getValue());
                    shaderEffect.setUniformValue("steps", stepsConfig.getValue());
                    shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, colorConfig.getValue().getAlpha() / 255.0f);
                    shaderEffect.setUniformValue("time", shaderTime);
                    shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
                    shaderEffect.setUniformValue("glow", glowConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("glowRadius", glowRadiusConfig.getValue());
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                    shaderTime += marbleFactorConfig.getValue();
                }, () ->
                {
                    ignoreEntityRender = true;
                    ((AccessorGameRenderer) mc.gameRenderer).hookRenderHand(mc.gameRenderer.getCamera(), event.getTickDelta(), event.getMatrices().peek().getPositionMatrix());
                    ignoreEntityRender = false;
                });
            }
//            case FLAME ->
//            {
//                final ManagedShaderEffect shaderEffect = Managers.SHADER.getFlameShaderEffect();
//                if (shaderEffect == null)
//                {
//                    return;
//                }
//                Managers.SHADER.applyShader(shaderEffect, () ->
//                {
//                    shaderEffect.setUniformValue("resolution", (float) mc.getWindow().getScaledWidth(), (float) mc.getWindow().getScaledHeight());
//                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
//                    shaderEffect.setUniformValue("samples", qualityConfig.getValue());
//                    shaderEffect.setUniformValue("steps", stepsConfig.getValue());
//                    shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, colorConfig.getValue().getAlpha() / 255.0f);
//                    shaderEffect.setUniformValue("time", shaderTime);
//                    shaderEffect.setUniformValue("fastOutline", fastOutlineConfig.getValue() ? 1 : 0);
//                    shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
//                    shaderEffect.setUniformValue("glow", glowConfig.getValue() ? 1 : 0);
//                    shaderEffect.setUniformValue("glowRadius", glowRadiusConfig.getValue());
//                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
//                    shaderTime += 0.005f;
//                }, () ->
//                {
//                    ignoreEntityRender = true;
//                    ((AccessorGameRenderer) mc.gameRenderer).hookRenderHand(mc.gameRenderer.getCamera(), event.getTickDelta(), event.getMatrices().peek().getPositionMatrix());
//                    ignoreEntityRender = false;
//                });
//            }
            case IMAGE ->
            {
                final ManagedShaderEffect shaderEffect = Managers.SHADER.getImageShaderEffect();
                if (shaderEffect == null)
                {
                    return;
                }
                Managers.SHADER.applyShader(shaderEffect, () ->
                {
                    GlStateManager._activeTexture(GL32C.GL_TEXTURE0 + 1);
                    GlStateManager._bindTexture(textureId);
                    shaderEffect.setUniformValue("texelSize", 1.0f / mc.getWindow().getScaledWidth(), 1.0f / mc.getWindow().getScaledHeight());
                    shaderEffect.setUniformValue("samples", qualityConfig.getValue());
                    shaderEffect.setUniformValue("steps", stepsConfig.getValue());
                    shaderEffect.setUniformValue("imageTexture", 1);
                    shaderEffect.setUniformValue("color", colorConfig.getValue().getRed() / 255.0f, colorConfig.getValue().getGreen() / 255.0f, colorConfig.getValue().getBlue() / 255.0f, colorConfig.getValue().getAlpha() / 255.0f);
                    shaderEffect.setUniformValue("fastOutline", fastOutlineConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("radius", outlineConfig.getValue() ? lineWidthConfig.getValue() : 0.0f);
                    shaderEffect.setUniformValue("glow", glowConfig.getValue() ? 1 : 0);
                    shaderEffect.setUniformValue("glowRadius", glowRadiusConfig.getValue());
                    shaderEffect.render(mc.getRenderTickCounter().getTickDelta(true));
                }, () ->
                {
                    ignoreEntityRender = true;
                    ((AccessorGameRenderer) mc.gameRenderer).hookRenderHand(mc.gameRenderer.getCamera(), event.getTickDelta(), event.getMatrices().peek().getPositionMatrix());
                    ignoreEntityRender = false;
                });
            }
        }
    }

    /**
     * Loads the shader image from memory. The image is loaded from the Shoreline folder.
     */
    private void loadShaderImage()
    {
        try
        {
            ByteBuffer data = null;
            String[] fileFormats = new String[] {"png", "jpg"};
            for (String fileFormat : fileFormats)
            {
                File shaderFile = Shoreline.CONFIG.getClientDirectory().resolve("shader." + fileFormat).toFile();
                if (shaderFile.exists())
                {
                    FileInputStream fileInputStream = new FileInputStream(shaderFile);
                    data = TextureUtil.readResource(fileInputStream);
                    break;
                }
                else
                {
                    Optional<Resource> optional = mc.getResourceManager().getResource(Identifier.of("shoreline", "shaders/shader." + fileFormat));
                    if (optional.isEmpty() || optional.get().getInputStream() == null)
                    {
                        continue;
                    }
                    data = TextureUtil.readResource(optional.get().getInputStream());
                    break;
                }
            }
            if (data == null)
            {
                return;
            }

            data.rewind();
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);

                STBImage.stbi_set_flip_vertically_on_load(true);
                ByteBuffer image = STBImage.stbi_load_from_memory(data, width, height, comp, 3);
                if (image == null)
                {
                    return;
                }

                textureId = GlStateManager._genTexture();
                GlStateManager._activeTexture(GL32C.GL_TEXTURE0);
                GlStateManager._bindTexture(textureId);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_SWAP_BYTES, GL32C.GL_FALSE);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_LSB_FIRST, GL32C.GL_FALSE);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_ROW_LENGTH, 0);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_IMAGE_HEIGHT, 0);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_SKIP_ROWS, 0);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_SKIP_PIXELS, 0);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_SKIP_IMAGES, 0);
                GlStateManager._pixelStore(GL32C.GL_UNPACK_ALIGNMENT, 4);
                GlStateManager._texParameter(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_WRAP_S, GL32C.GL_REPEAT);
                GlStateManager._texParameter(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_WRAP_T, GL32C.GL_REPEAT);
                GlStateManager._texParameter(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_MIN_FILTER, GL32C.GL_NEAREST);
                GlStateManager._texParameter(GL32C.GL_TEXTURE_2D, GL32C. GL_TEXTURE_MAG_FILTER, GL32C.GL_NEAREST);

                image.rewind();
                GL32C.glTexImage2D(GL32C.GL_TEXTURE_2D, 0, GL32C.GL_RGB, width.get(0), height.get(0), 0, GL32C.GL_RGB, GL32C.GL_UNSIGNED_BYTE, image);

                STBImage.stbi_image_free(image);
                STBImage.stbi_set_flip_vertically_on_load(false);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean checkShaders(Entity entity)
    {
        if (entity instanceof PlayerEntity && playersConfig.getValue())
        {
            return selfConfig.getValue() && (!mc.options.getPerspective().isFirstPerson() || FreecamModule.getInstance().isEnabled()) || entity != mc.player;
        }
        return (!entity.isInvisible() || invisiblesConfig.getValue())
                && (EntityUtil.isMonster(entity) && monstersConfig.getValue()
                || EntityUtil.isNeutral(entity) && neutralsConfig.getValue()
                || EntityUtil.isPassive(entity) && animalsConfig.getValue())
                || entity instanceof EndCrystalEntity && otherConfig.getValue()
                || entity instanceof ItemEntity && itemsConfig.getValue()
                || entity instanceof ExperienceBottleEntity && projectilesConfig.getValue()
                || entity instanceof EnderPearlEntity && projectilesConfig.getValue();
    }

    private boolean checkStorageShaders(BlockEntity blockEntity)
    {
        return blockEntity instanceof ChestBlockEntity && chestsConfig.getValue()
                || blockEntity instanceof EnderChestBlockEntity && echestsConfig.getValue()
                || blockEntity instanceof ShulkerBoxBlockEntity && shulkersConfig.getValue();
    }

    private enum ShaderMode
    {
        DEFAULT,
        GRADIENT,
        MARBLE,
        // FLAME,
        IMAGE,
        OFF
    }
}
