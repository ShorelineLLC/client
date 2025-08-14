package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.render.NightVisionEvent;
import net.shoreline.client.impl.event.render.WorldGammaEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class FullbrightModule extends Toggleable
{
    Config<Brightness> modeConfig = new EnumConfig.Builder<Brightness>("Mode")
            .setValues(Brightness.values())
            .setDescription("The client world brightness mode")
            .setDefaultValue(Brightness.GAMMA).build();

    public FullbrightModule()
    {
        super("Fullbright", "Brightens the world", GuiCategory.RENDER);
    }

    @EventListener
    public void onWorldGamma(WorldGammaEvent event)
    {
        if (modeConfig.getValue() == Brightness.GAMMA)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onNightVision(NightVisionEvent event)
    {
        if (modeConfig.getValue() == Brightness.POTION)
        {
            event.cancel();
        }
    }

    public enum Brightness
    {
        GAMMA,
        POTION
    }
}
