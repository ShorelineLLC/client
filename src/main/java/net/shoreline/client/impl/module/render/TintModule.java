package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.LoadingEvent;
import net.shoreline.client.impl.event.render.WorldTintEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class TintModule extends Toggleable
{
    Config<Color> tintColor = new ColorConfig.Builder("Tint")
            .setDescription("The color of the world tint")
            .setDefaultValue(Color.RED).build();
    Config<Boolean> lightConfig = new BooleanConfig.Builder("Light")
            .setDescription("Change the color of world light")
            .setDefaultValue(false).build();
    Config<Boolean> foliageConfig = new BooleanConfig.Builder("Foliage")
            .setDescription("Change the color of foliage")
            .setDefaultValue(false).build();
    Config<Boolean> waterConfig = new BooleanConfig.Builder("Water")
            .setDescription("Change the color of water")
            .setDefaultValue(false).build();
    Config<Boolean> lavaConfig = new BooleanConfig.Builder("Lava")
            .setDescription("Change the color of lava")
            .setDefaultValue(false).build();

    public TintModule()
    {
        super("Tint", new String[] {"Ambience"}, "Change the world color tint", GuiCategory.RENDER);
    }

    @Override
    public void onToggle()
    {
        reloadWorld();
    }

    @EventListener
    public void onFinishedLoading(LoadingEvent.Finished event)
    {
        for (Config<?> config : getConfigs())
        {
            if (config != lightConfig)
            {
                config.addListener(v -> reloadWorld());
            }
        }
    }

    @EventListener
    public void onLightTint(WorldTintEvent.Light event)
    {
        if (lightConfig.getValue())
        {
            event.cancel();
            event.setColor(tintColor.getValue());
        }
    }

    @EventListener
    public void onFoliageTint(WorldTintEvent.Foliage event)
    {
        if (foliageConfig.getValue())
        {
            event.cancel();
            event.setColor(tintColor.getValue());
        }
    }

    @EventListener
    public void onWaterTint(WorldTintEvent.Water event)
    {
        if (waterConfig.getValue())
        {
            event.cancel();
            event.setColor(tintColor.getValue());
        }
    }

    @EventListener
    public void onLavaTint(WorldTintEvent.Lava event)
    {
        if (lavaConfig.getValue())
        {
            event.cancel();
            event.setColor(tintColor.getValue());
        }
    }

    private void reloadWorld()
    {
        if (mc.worldRenderer != null)
        {
            int x = (int) mc.player.getX();
            int y = (int) mc.player.getY();
            int z = (int) mc.player.getZ();
            int d = mc.options.getViewDistance().getValue() * 16;
            mc.worldRenderer.scheduleBlockRenders(x - d, y - d, z - d, x + d, y + d, z + d);
        }
    }
}
