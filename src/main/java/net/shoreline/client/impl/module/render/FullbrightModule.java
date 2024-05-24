package net.shoreline.client.impl.module.render;

import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.eventbus.StageEvent;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.color.world.BiomeColorEvent;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.event.render.LightmapGammaEvent;

import java.awt.*;

/**
 * @author linus
 * @since 1.0
 */
public class FullbrightModule extends ToggleModule {

    Config<Brightness> brightnessConfig = register(new EnumConfig<>("Mode", "Mode for world brightness", Brightness.GAMMA, Brightness.values()));
    Config<Boolean> biomeConfig = register(new BooleanConfig("Biome", "Colors the light of the biome", false));
    Config<Color> biomeColorConfig = register(new ColorConfig("BiomeColor", "The color of the biome", Color.RED, false, true, () -> biomeConfig.getValue()));
    Config<Boolean> waterConfig = register(new BooleanConfig("Water", "Colors the water", false));
    Config<Color> waterColorConfig = register(new ColorConfig("WaterColor", "The color of the water", Color.RED, false, true, () -> waterConfig.getValue()));
    Config<Boolean> grassConfig = register(new BooleanConfig("Grass", "Colors the grass", false));
    Config<Color> grassColorConfig = register(new ColorConfig("GrassColor", "The color of the grass", Color.RED, false, true, () -> grassConfig.getValue()));

    public FullbrightModule() {
        super("Fullbright", "Brightens the world", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null && mc.world != null
                && brightnessConfig.getValue() == Brightness.POTION) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0)); // INFINITE
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null && mc.world != null
                && brightnessConfig.getValue() == Brightness.POTION) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event) {
        onDisable();
        onEnable();
    }

    @EventListener
    public void onLightmapGamma(LightmapGammaEvent event) {
        if (brightnessConfig.getValue() == Brightness.GAMMA) {
            event.cancel();
            event.setGamma(0xffffffff);
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event) {
        if (mc.player != null && brightnessConfig == event.getConfig()
                && event.getStage() == StageEvent.EventStage.POST
                && brightnessConfig.getValue() != Brightness.POTION) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (brightnessConfig.getValue() == Brightness.POTION
                && !mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0));
        }
    }

    @EventListener
    public void onBiomeColor(BiomeColorEvent event) {
        if (biomeConfig.getValue() && event.getColorResolver() == BiomeColors.FOLIAGE_COLOR) {
            event.cancel();
            event.setColor(biomeColorConfig.getValue());
        }
        else if (waterConfig.getValue() && event.getColorResolver() == BiomeColors.WATER_COLOR) {
            event.cancel();
            event.setColor(waterColorConfig.getValue());
        }
        else if (grassConfig.getValue() && event.getColorResolver() == BiomeColors.GRASS_COLOR) {
            event.cancel();
            event.setColor(grassColorConfig.getValue());
        }
    }

    public enum Brightness {
        GAMMA,
        POTION
    }
}
