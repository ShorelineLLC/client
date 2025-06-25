package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.StringConfig;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;

public class HeadlessMcModule extends Concurrent
{
    Config<String> ipConfig = new StringConfig.Builder("IP")
            .setDefaultValue("127.0.0.1").build();
    Config<String> portConfig = new StringConfig.Builder("Port")
            .setDefaultValue("25565").build();

    public HeadlessMcModule()
    {
        super("HeadlessMc", "Allows you to connect to a HeadlessMC instance", GuiCategory.CLIENT);
    }
}
