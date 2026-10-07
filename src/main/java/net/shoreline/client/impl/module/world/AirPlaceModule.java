package net.shoreline.client.impl.module.world;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class AirPlaceModule extends Toggleable
{
    public static AirPlaceModule INSTANCE;

    Config<Boolean> grimConfig = new BooleanConfig.Builder("Grim")
            .setDescription("Place blocks in the air on Grim servers")
            .setDefaultValue(false).build();

    public AirPlaceModule()
    {
        super("AirPlace", "Place blocks in the air", GuiCategory.WORLD);
        INSTANCE = this;
    }

    public boolean isGrim()
    {
        return grimConfig.getValue();
    }

    public boolean isForceAirPlace()
    {
        return false;
    }
}
