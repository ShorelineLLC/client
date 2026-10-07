package net.shoreline.client.impl.module.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.LoadingEvent;
import net.shoreline.client.impl.event.render.RenderEntityWorldEvent;
import net.shoreline.client.impl.event.render.RenderShaderEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.render.item.RenderHandEvent;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.client.impl.render.manager.ShaderManager;
import net.shoreline.client.impl.render.shader.ShaderEffect;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.Color;

public class ShadersModule extends RenderModule
{
    public static ShadersModule INSTANCE;

    public Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setMin(0.0f).setDefaultValue(30.0f).setMax(250.0f)
            .setDescription("If entity is within this range we apply shaders").build();
    Config<Boolean> depthConfig = new BooleanConfig.Builder("ThroughWalls")
            .setDescription("Renders shaders through walls")
            .setDefaultValue(true).build();

    Config<Boolean> handsConfig = new BooleanConfig.Builder("Hands")
            .setDescription("Render shaders over hands")
            .setDefaultValue(true).build();
    Config<Boolean> playersConfig = new BooleanConfig.Builder("Players")
            .setDescription("Render shaders over other players")
            .setDefaultValue(true).build();
    Config<Boolean> selfConfig = new BooleanConfig.Builder("Self")
            .setDescription("Render shaders over the player")
            .setDefaultValue(true).build();
    Config<Boolean> crystalsConfig = new BooleanConfig.Builder("Crystals")
            .setDescription("Render shaders over crystals")
            .setDefaultValue(true).build();
    Config<Boolean> itemsConfig = new BooleanConfig.Builder("Items")
            .setDescription("Render shaders over items")
            .setDefaultValue(true).build();
    Config<Boolean> xpConfig = new BooleanConfig.Builder("XP")
            .setDescription("Render shaders over xp bottles")
            .setDefaultValue(true).build();
    Config<Boolean> pearlsConfig = new BooleanConfig.Builder("Pearls")
            .setDescription("Render shaders over pearls")
            .setDefaultValue(true).build();
    Config<Boolean> passiveConfig = new BooleanConfig.Builder("Passive")
            .setDescription("Render shaders over hands")
            .setDefaultValue(true).build();
    Config<Boolean> hostilesConfig = new BooleanConfig.Builder("Hostiles")
            .setDescription("Render shaders over hands")
            .setDefaultValue(true).build();
    public Config<Void> renderConfig = new ConfigGroup.Builder("Target")
            .addAll(handsConfig, playersConfig, selfConfig, crystalsConfig, itemsConfig,
                    xpConfig, pearlsConfig, passiveConfig, hostilesConfig).build();

    Config<Shaders> outlineMode = new EnumConfig.Builder<Shaders>("OutlineMode")
            .setValues(Shaders.values())
            .setDescription("Outlines the entity")
            .setDefaultValue(Shaders.DEFAULT).build();
    Config<Float> outlineWidth = new NumberConfig.Builder<Float>("OutlineWidth")
            .setMin(0.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setDescription("The width of the outline").build();
    Config<Float> outlineOpacity = new NumberConfig.Builder<Float>("OutlineOpacity")
            .setMin(0.01f).setDefaultValue(1.0f).setMax(1.0f)
            .setDescription("Opacity for the outline").build();
    Config<Boolean> glowInside = new BooleanConfig.Builder("GlowInwards")
            .setDescription("Glow inside the fill")
            .setVisible(() -> outlineMode.getValue() == Shaders.BLOOM)
            .setDefaultValue(false).build();
    Config<Float> glowConfig = new NumberConfig.Builder<Float>("GlowFactor")
            .setMin(0.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setVisible(() -> outlineMode.getValue() == Shaders.BLOOM).build();
    Config<Integer> qualityConfig = new NumberConfig.Builder<Integer>("GlowQuality")
            .setMin(1).setMax(5).setDefaultValue(2)
            .setVisible(() -> outlineMode.getValue() == Shaders.BLOOM).build();
    Config<Void> outlineConfig = new ConfigGroup.Builder("Outline")
            .addAll(outlineMode, outlineWidth, outlineOpacity, glowInside, glowConfig, qualityConfig).build();

    Config<Fill> fillMode = new EnumConfig.Builder<Fill>("FillMode")
            .setValues(Fill.values())
            .setDescription("Fills the entity")
            .setDefaultValue(Fill.DEFAULT).build();
    Config<Float> fillOpacity = new NumberConfig.Builder<Float>("FillOpacity")
            .setMin(0.0f).setDefaultValue(0.5f).setMax(1.0f)
            .setDescription("Opacity for the shader fill").build();
    Config<Float> gradientFactor = new NumberConfig.Builder<Float>("GradientFactor")
            .setMin(0.1f).setDefaultValue(5.0f).setMax(10.0f)
            .setVisible(() -> fillMode.getValue() == Fill.GRADIENT)
            .setDescription("The separation between gradient layers").build();
    Config<Color> gradientColor = new ColorConfig.Builder("GradientColor")
            .setTransparency(true).setDescription("The color for the gradient")
            .setVisible(() -> fillMode.getValue() == Fill.GRADIENT)
            .setDefaultValue(Color.WHITE).build();
    Config<Integer> flowSpeed = new NumberConfig.Builder<Integer>("FlowLayers")
            .setMin(1).setDefaultValue(10).setMax(10)
            .setVisible(() -> fillMode.getValue() == Fill.FLOW)
            .setDescription("The separation between gradient layers").build();
    Config<Float> flowFactor = new NumberConfig.Builder<Float>("FlowFactor")
            .setMin(0.1f).setDefaultValue(0.6f).setMax(1.0f)
            .setVisible(() -> fillMode.getValue() == Fill.FLOW)
            .setDescription("The separation between gradient layers").build();
    Config<Integer> liquidSpeed = new NumberConfig.Builder<Integer>("LiquidLayers")
            .setMin(1).setDefaultValue(5).setMax(10)
            .setVisible(() -> fillMode.getValue() == Fill.LIQUID)
            .setDescription("The separation between gradient layers").build();
    Config<Float> liquidFactor = new NumberConfig.Builder<Float>("LiquidFactor")
            .setMin(1.0f).setDefaultValue(5.0f).setMax(10.0f)
            .setVisible(() -> fillMode.getValue() == Fill.LIQUID)
            .setDescription("The separation between gradient layers").build();
    Config<Void> fillConfig = new ConfigGroup.Builder("Fill")
            .addAll(fillMode, fillOpacity, gradientFactor, gradientColor, flowSpeed, flowFactor, liquidSpeed, liquidFactor).build();

    private ShaderManager shaderManager;

    public ShadersModule()
    {
        super("Shaders", "Renders shaders over entities", GuiCategory.RENDER);
        INSTANCE = this;

        depthConfig.addObserver(v ->
        {
            if (shaderManager != null)
            {
                shaderManager.clearCache();
            }
        });
    }

    @Override
    public void onEnable()
    {
        if (shaderManager == null && mc.getWindow() != null)
        {
            shaderManager = new ShaderManager(INSTANCE);
        }
    }

    @EventListener
    public void onFinishedLoading(LoadingEvent.Finished event)
    {
        shaderManager = new ShaderManager(INSTANCE);
    }

    @EventListener
    public void onRenderShader(RenderShaderEvent event)
    {
        if (!checkNull() && shaderManager != null)
        {
            shaderManager.begin();
        }
    }

    @EventListener
    public void onResize(RenderWorldEvent.Resized event)
    {
        if (shaderManager != null)
        {
            shaderManager.resize(event.getWidth(), event.getHeight());
        }
    }

    @EventListener
    public void onRenderShader(RenderShaderEvent.Post event)
    {
        if (!checkNull() && shaderManager != null)
        {
            shaderManager.render(outlineMode.getValue().updateEffect(INSTANCE));
        }
    }

    @EventListener
    public void onRenderEntity(RenderEntityWorldEvent event)
    {
        if (!shouldRenderShader(event.getEntity()) || shaderManager == null)
        {
            return;
        }

        Color color = SocialsModule.INSTANCE.getEntityColor(
                event.getEntity(), ThemeModule.INSTANCE.getPrimaryColor());

        event.cancel();
        event.setVertexConsumerProvider(shaderManager.createVertexConsumer(
                event.getVertexConsumerProvider(), color));
    }

    @EventListener
    public void onRenderEntityPost(RenderEntityWorldEvent.Post event)
    {
        if (shaderManager != null)
        {
            shaderManager.draw();
        }
    }

    @EventListener
    public void onRenderHand(RenderHandEvent event)
    {
        if (handsConfig.getValue() && shaderManager != null)
        {
            event.cancel();
            event.setVertexConsumerProvider(shaderManager.createVertexConsumer(
                    event.getVertexConsumerProvider(), ThemeModule.INSTANCE.getPrimaryColor()));
        }
    }

    @EventListener
    public void onRenderHandPost(RenderHandEvent.Post event)
    {
        if (handsConfig.getValue() && shaderManager != null)
        {
            shaderManager.draw();
        }
    }

    private boolean shouldRenderShader(Entity entity)
    {
        if (MathHelper.square(rangeConfig.getValue()) < entity.squaredDistanceTo(getCameraPos()))
        {
            return false;
        }

        return switch (entity)
        {
            case PlayerEntity player when player != mc.player ? playersConfig.getValue() : selfConfig.getValue() -> true;
            case Monster monster when hostilesConfig.getValue() -> true;
            case AnimalEntity animalEntity when passiveConfig.getValue() -> true;
            case ItemEntity itemEntity when itemsConfig.getValue() -> true;
            case ExperienceBottleEntity xpEntity when xpConfig.getValue() -> true;
            case EnderPearlEntity pearlEntity when pearlsConfig.getValue() -> true;
            default -> entity instanceof EndCrystalEntity && crystalsConfig.getValue();
        };
    }

    public boolean getDepth()
    {
        return depthConfig.getValue();
    }

    public enum Fill
    {
        DEFAULT,
        GRADIENT,
        FLOW,
        LIQUID,
        RAINBOW
    }

    public enum Shaders
    {
        DEFAULT("outline"),
        BLOOM("bloom")
                {
                    @Override
                    protected ShaderEffect updateEffect(ShadersModule shadersModule)
                    {
                        effect.addIntUniform("u_GlowInside", shadersModule.glowInside.getValue() ? 1 : 0);
                        effect.addIntUniform("u_GlowQuality", shadersModule.qualityConfig.getValue());
                        effect.addFltUniform("u_GlowMultiplier", shadersModule.glowConfig.getValue());
                        return super.updateEffect(shadersModule);
                    }
                };

        protected final ShaderEffect effect;
        private static final long startTime = System.currentTimeMillis();

        Shaders(String shaderName)
        {
            this.effect = new ShaderEffect(shaderName);
        }

        protected ShaderEffect updateEffect(ShadersModule shadersModule)
        {
            effect.addFltUniform("u_ShaderTime", (float) (System.currentTimeMillis() - startTime));
            effect.addVec2Uniform("u_Resolution",
                    mc.getWindow().getFramebufferWidth(),
                    mc.getWindow().getFramebufferHeight());
            effect.addFltUniform("u_Width", shadersModule.outlineWidth.getValue());
            effect.addIntUniform("u_FillMode", shadersModule.fillMode.getValue().ordinal());
            effect.addFltUniform("u_FillAlpha", shadersModule.fillOpacity.getValue());
            effect.addFltUniform("u_GradientFactor", shadersModule.gradientFactor.getValue() * 16.0f);

            Color gradientColor = shadersModule.gradientColor.getValue();
            effect.addVec4Uniform("u_GradientColor",
                    gradientColor.getRed() / 255.0f,
                    gradientColor.getGreen() / 255.0f,
                    gradientColor.getBlue() / 255.0f,
                    gradientColor.getAlpha() / 255.0f);

            effect.addFltUniform("u_FlowSpeed", shadersModule.flowSpeed.getValue());
            effect.addFltUniform("u_FlowFactor", shadersModule.flowFactor.getValue());
            effect.addFltUniform("u_LiquidIntensity", shadersModule.liquidSpeed.getValue());
            effect.addFltUniform("u_LiquidFactor", shadersModule.liquidFactor.getValue());
            effect.addFltUniform("u_OutlineAlpha", shadersModule.outlineOpacity.getValue());
            return effect;
        }
    }
}
