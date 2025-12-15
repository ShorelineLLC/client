package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.irc.CapeType;

@Getter
public class CapesModule extends Toggleable
{
    public static CapesModule INSTANCE;

    Config<CapeType> capeType = new EnumConfig.Builder<CapeType>("Cape")
            .setValues(CapeType.values())
            .setDescription("The cape for the player")
            .setDefaultValue(CapeType.BLACK).build();
    Config<Boolean> optifineCapes = new BooleanConfig.Builder("Optifine")
            .setDescription("Shows optifine capes")
            .setDefaultValue(true).build();

    public CapesModule()
    {
        super("Capes", "Shows client capes", GuiCategory.CLIENT);
        INSTANCE = this;
    }
}
