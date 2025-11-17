package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;

public class PearlBlockerModule extends ObsidianPlacerModule
{
    Config<Float> placeRange = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("Range to place blocks").build();
    Config<Boolean> blocksConfig = new BooleanConfig.Builder("Blocks")
            .setDescription("Place blocks to block pearls")
            .setDefaultValue(true).build();
    Config<Boolean> itemFrameConfig = new BooleanConfig.Builder("ItemFrame")
            .setDescription("Place item frames to block pearls")
            .setDefaultValue(false).build();

    public PearlBlockerModule()
    {
        super("PearlBlocker", "Blocks thrown ender pearls", GuiCategory.COMBAT);
    }

}
