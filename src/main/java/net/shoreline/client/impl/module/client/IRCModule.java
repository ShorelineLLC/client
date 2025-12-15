package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

@Getter
public class IRCModule extends Toggleable
{
    public static IRCModule INSTANCE;

    Config<Boolean> dmsOnly = new BooleanConfig.Builder("OnlyDMS")
            .setDescription("Only show direct messages")
            .setDefaultValue(false).build();

    public IRCModule()
    {
        super("IRC", "The client online chat", GuiCategory.CLIENT);
        INSTANCE = this;
    }
}
