package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;
import net.shoreline.eventbus.annotation.EventListener;

public class FillerModule extends ObsidianPlacerModule
{
    public static FillerModule INSTANCE;

    Config<Float> placeRange = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("Range to place blocks").build();
    Config<FillMode> fillMode = new EnumConfig.Builder<FillMode>("Mode")
            .setValues(FillMode.values())
            .setDescription("The area to fill")
            .setDefaultValue(FillMode.HOLES).build();
    Config<Boolean> doublesConfig = new BooleanConfig.Builder("Doubles")
            .setDescription("Fills in double holes")
            .setVisible(() -> fillMode.getValue() == FillMode.HOLES)
            .setDefaultValue(false).build();
    Config<Boolean> autoDisable = new BooleanConfig.Builder("AutoDisable")
            .setDescription("Disables after filling")
            .setDefaultValue(false).build();

    public FillerModule()
    {
        super("Filler", new String[] {"HoleFill"}, "Fills in blocks around you", GuiCategory.COMBAT);
        INSTANCE = this;
    }



    public boolean shouldGetDoubles()
    {
        return isEnabled() && doublesConfig.getValue();
    }

    public enum FillMode
    {
        HOLES,
        FLATTEN
    }
}
