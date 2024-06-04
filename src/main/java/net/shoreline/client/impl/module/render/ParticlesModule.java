package net.shoreline.client.impl.module.render;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
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

    Config<TotemParticle> totemConfig = register(new EnumConfig<>("Totem", "Renders totem particles", TotemParticle.OFF, TotemParticle.values()));
    Config<Color> totemColorConfig = register(new ColorConfig("TotemColor", "Color of the totem particles", new Color(25, 120, 0), false, false, () -> totemConfig.getValue() == TotemParticle.COLOR));
    Config<Boolean> fireworkConfig = register(new BooleanConfig("Firework", "Renders firework particles", false));
    Config<Boolean> potionConfig = register(new BooleanConfig("Effects", "Renders potion effect particles", true));
    Config<Boolean> bottleConfig = register(new BooleanConfig("BottleSplash", "Render bottle splash particles", true));
    Config<Boolean> portalConfig = register(new BooleanConfig("Portal", "Render portal particles", true));

    public ParticlesModule()
    {
        super("Particles", "Change the rendering of particles", ModuleCategory.RENDER);
    }

    @EventListener
    public void onParticle(ParticleEvent event)
    {
        if (potionConfig.getValue() && event.getParticleType() == ParticleTypes.ENTITY_EFFECT
                || fireworkConfig.getValue() && event.getParticleType() == ParticleTypes.FIREWORK
                || bottleConfig.getValue() && (event.getParticleType() == ParticleTypes.EFFECT || event.getParticleType() == ParticleTypes.INSTANT_EFFECT)
                || portalConfig.getValue() && event.getParticleType() == ParticleTypes.PORTAL)
        {
            event.cancel();
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
            event.setColor(new Color(MathHelper.clamp(r + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f),
                    MathHelper.clamp(g + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f),
                    MathHelper.clamp(b + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f)));
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

    private enum TotemParticle
    {
        OFF,
        REMOVE,
        COLOR
    }
}
