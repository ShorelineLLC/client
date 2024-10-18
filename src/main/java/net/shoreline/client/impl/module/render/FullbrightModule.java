package net.shoreline.client.impl.module.render;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.event.render.LightmapGammaEvent;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class FullbrightModule extends ToggleModule
{
    Config<Brightness> brightnessConfig = register(new EnumConfig<>("Mode", "Mode for world brightness", Brightness.GAMMA, Brightness.values()));

    public FullbrightModule()
    {
        super("Fullbright", "Brightens the world", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable()
    {
        if (mc.player != null && mc.world != null
                && brightnessConfig.getValue() == Brightness.POTION)
        {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0)); // INFINITE
        }
    }

    @Override
    public void onDisable()
    {
        if (mc.player != null && mc.world != null
                && brightnessConfig.getValue() == Brightness.POTION)
        {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event)
    {
        onDisable();
        onEnable();
    }

    @EventListener
    public void onLightmapGamma(LightmapGammaEvent event)
    {
        if (brightnessConfig.getValue() == Brightness.GAMMA)
        {
            event.cancel();
            event.setGamma(0xffffffff);
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (mc.player != null && brightnessConfig == event.getConfig()
                && event.getStage() == StageEvent.EventStage.POST
                && brightnessConfig.getValue() != Brightness.POTION)
        {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (brightnessConfig.getValue() == Brightness.POTION
                && !mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION))
        {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0));
        }
    }

    public enum Brightness
    {
        GAMMA,
        POTION
    }
}
