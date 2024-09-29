package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.biome.BiomeColorEvent;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.render.AmbientColorEvent;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.awt.*;

public class AmbienceModule extends ToggleModule
{
    private static AmbienceModule INSTANCE;

    Config<Boolean> lightConfig = register(new BooleanConfig("Light", "Colors the light", false));
    Config<Color> lightColorConfig = register(new ColorConfig("LightColor", "The color of the light", Color.RED, false, true, () -> lightConfig.getValue()));
    Config<Boolean> grassConfig = register(new BooleanConfig("Grass", "Colors the grass", false));
    Config<Color> grassColorConfig = register(new ColorConfig("GrassColor", "The color of the grass", Color.RED, false, true, () -> grassConfig.getValue()));
    Config<Boolean> foliageConfig = register(new BooleanConfig("Foliage", "Colors the foliage", false));
    Config<Color> foliageColorConfig = register(new ColorConfig("FoliageColor", "The color of the foliage", Color.RED, false, true, () -> foliageConfig.getValue()));
    Config<Boolean> waterConfig = register(new BooleanConfig("Water", "Colors the water", false));
    Config<Color> waterColorConfig = register(new ColorConfig("WaterColor", "The color of the water", Color.RED, false, true, () -> waterConfig.getValue()));
//    Config<Boolean> lavaConfig = register(new BooleanConfig("Lava", "Colors the lava", false));
//    Config<Color> lavaColorConfig = register(new ColorConfig("LavaColor", "The color of the lava", Color.RED, false, true, () -> lavaConfig.getValue()));

    public AmbienceModule()
    {
        super("Ambience", "Changes rendering of world", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    public static AmbienceModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onEnable()
    {
        if (mc.world == null)
        {
            return;
        }
        mc.worldRenderer.reload();
    }

    @Override
    public void onDisable()
    {
        if (mc.world == null)
        {
            return;
        }
        mc.worldRenderer.reload();
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (mc.world != null && event.getConfig().getContainer() == this
                && (event.getConfig() != lightConfig || event.getConfig() != lightColorConfig)
                && event.getStage() == StageEvent.EventStage.POST)
        {
            mc.worldRenderer.reload();
        }
    }

    @EventListener
    public void onAmbientColor(AmbientColorEvent event)
    {
        if (lightConfig.getValue())
        {
            event.cancel();
            event.setColor(lightColorConfig.getValue());
        }
    }

    @EventListener
    public void onGrassColor(BiomeColorEvent.Grass event)
    {
        if (grassConfig.getValue())
        {
            event.cancel();
            event.setColor(grassColorConfig.getValue());
        }
    }

    @EventListener
    public void onFoliageColor(BiomeColorEvent.Foliage event)
    {
        if (foliageConfig.getValue())
        {
            event.cancel();
            event.setColor(foliageColorConfig.getValue());
        }
    }

    @EventListener
    public void onWaterColor(BiomeColorEvent.Water event)
    {
        if (waterConfig.getValue())
        {
            event.cancel();
            event.setColor(waterColorConfig.getValue());
        }
    }

//    @EventListener
//    public void onLavaColor(BiomeColorEvent.Lava event)
//    {
//        if (lavaConfig.getValue())
//        {
//            event.cancel();
//            event.setColor(lavaColorConfig.getValue());
//        }
//    }
//
//    public int getLavaColor()
//    {
//        return lavaColorConfig.getValue().getRGB();
//    }
}
