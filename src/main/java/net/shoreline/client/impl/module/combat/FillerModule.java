package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class FillerModule extends Toggleable
{
    public static FillerModule INSTANCE;

    Config<Float> placeRange = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("Range to place blocks").build();
    Config<Boolean> holesConfig = new BooleanConfig.Builder("Holes")
            .setDescription("Fills in holes around you")
            .setDefaultValue(false).build();
    Config<Boolean> doublesConfig = new BooleanConfig.Builder("Doubles")
            .setDescription("Fills in double holes")
            .setVisible(() -> holesConfig.getValue())
            .setDefaultValue(false).build();

    public FillerModule()
    {
        super("Filler", "Fills in blocks around you", GuiCategory.COMBAT);
        INSTANCE = this;
    }

    public boolean shouldGetDoubles()
    {
        return isEnabled() && doublesConfig.getValue();
    }
}
