package net.shoreline.client.impl.module.render;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.particle.BlockBreakParticleEvent;
import net.shoreline.client.impl.event.particle.EmitParticleEvent;
import net.shoreline.client.impl.event.particle.ParticleEvent;
import net.shoreline.client.impl.event.particle.TotemParticleEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

/**
 * @author Shoreline
 * @since 1.0
 */
public class ParticlesModule extends ToggleModule
{

    Config<TotemParticle> totemConfig = register(new EnumConfig<>("Totem", "Modify totem particle rendering", TotemParticle.OFF, TotemParticle.values()));
    Config<Color> totemColorConfig = register(new ColorConfig("TotemColor", "Color of the totem particles", new Color(25, 120, 0), false, false, () -> totemConfig.getValue() == TotemParticle.COLOR));
    Config<Integer> totemParticlesConfig = register(new NumberConfig<>("TotemParticles", "Max totem particles", 3, 16, 16, () -> totemConfig.getValue() != TotemParticle.REMOVE));
    Config<Integer> totemParticleTicksConfig = register(new NumberConfig<>("TotemParticleTicks", "Max totem particle ticks", 5, 30, 30, () -> totemConfig.getValue() != TotemParticle.REMOVE));
    Config<Boolean> explosionsConfig = register(new BooleanConfig("Explosions", "Prevents explosion particles from rendering", true));
    Config<Boolean> fireworkConfig = register(new BooleanConfig("Firework", "Prevents rendering of firework particles", false));
    Config<Boolean> potionConfig = register(new BooleanConfig("Effects", "Prevents rendering of potion effect particles", true));
    Config<Boolean> itemsConfig = register(new BooleanConfig("Items", "Prevents rendering of eating particles", false));
    Config<Boolean> bottleConfig = register(new BooleanConfig("BottleSplash", "Prevents rendering of bottle splash particles", true));
    Config<Boolean> portalConfig = register(new BooleanConfig("Portal", "Prevents rendering of portal particles", true));
    Config<Boolean> blockConfig = register(new BooleanConfig("Block", "Prevents block particles from rendering", false));
    Config<Boolean> blockBreakConfig = register(new BooleanConfig("BlockBreak", "Prevents block break particles from rendering", false));
    Config<Boolean> campfiresConfig = register(new BooleanConfig("Campfires", "Prevents campfire particles from rendering", false));
    Config<Boolean> obsidianTearConfig = register(new BooleanConfig("CryingObsidian", "Prevents obsidian tear particles from rendering", false));

    public ParticlesModule()
    {
        super("Particles", "Change the rendering of particles", ModuleCategory.RENDER);
    }

    @EventListener
    public void onParticle(ParticleEvent event)
    {
        if (potionConfig.getValue() && event.getParticleType() == ParticleTypes.ENTITY_EFFECT
                || explosionsConfig.getValue() && (event.getParticleType() == ParticleTypes.EXPLOSION ||event.getParticleType() == ParticleTypes.EXPLOSION_EMITTER)
                || fireworkConfig.getValue() && event.getParticleType() == ParticleTypes.FIREWORK
                || itemsConfig.getValue() && event.getParticleType() == ParticleTypes.ITEM
                || bottleConfig.getValue() && (event.getParticleType() == ParticleTypes.EFFECT || event.getParticleType() == ParticleTypes.INSTANT_EFFECT)
                || portalConfig.getValue() && event.getParticleType() == ParticleTypes.PORTAL
                || blockConfig.getValue() && event.getParticleType() == ParticleTypes.BLOCK
                || campfiresConfig.getValue() && event.getParticleType() == ParticleTypes.CAMPFIRE_COSY_SMOKE
                || obsidianTearConfig.getValue() && (event.getParticleType() == ParticleTypes.FALLING_OBSIDIAN_TEAR || event.getParticleType() == ParticleTypes.DRIPPING_OBSIDIAN_TEAR || event.getParticleType() == ParticleTypes.LANDING_OBSIDIAN_TEAR))
        {
            event.cancel();
        }
    }

    @EventListener
    public void onParticleEmitter(ParticleEvent.Emitter event)
    {
        if (totemConfig.getValue() == TotemParticle.REMOVE && event.getParticleType() == ParticleTypes.TOTEM_OF_UNDYING)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onEmitParticle(EmitParticleEvent event)
    {
        if (totemConfig.getValue() != TotemParticle.REMOVE && event.getParticleType() == ParticleTypes.TOTEM_OF_UNDYING)
        {
            event.cancel();
            event.setParticleCount(totemParticlesConfig.getValue());
            event.setParticleTime(totemParticleTicksConfig.getValue());
        }
    }

    @EventListener
    public void onTotemParticle(TotemParticleEvent event)
    {
        if (totemConfig.getValue() == TotemParticle.COLOR)
        {
            event.cancel();
            Color color = totemColorConfig.getValue();
            float r = color.getRed() / 255.0f;
            float g = color.getGreen() / 255.0f;
            float b = color.getBlue() / 255.0f;
            if (RANDOM.nextInt(4) == 0)
            {
                float r2 = (color.getRed() * 0.7f) / 255.0f;
                float g2 = (color.getGreen() * 0.7f) / 255.0f;
                float b2 = (color.getBlue() * 0.7f) / 255.0f;
                event.setColor(new Color(MathHelper.clamp(r2, 0.0f, 1.0f),
                        MathHelper.clamp(g2, 0.0f, 1.0f),
                        MathHelper.clamp(b2, 0.0f, 1.0f)));
            }
            else
            {
                event.setColor(new Color(MathHelper.clamp(r + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f),
                        MathHelper.clamp(g + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f),
                        MathHelper.clamp(b + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f)));
            }
        }
    }

    @EventListener
    public void onBlockBreakParticle(BlockBreakParticleEvent event)
    {
        if (blockBreakConfig.getValue())
        {
            event.cancel();
        }
    }

    private enum TotemParticle
    {
        OFF,
        REMOVE,
        COLOR
    }
}
