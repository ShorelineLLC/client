package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.StringConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class HeadlessMCModule extends Toggleable
{
    public static HeadlessMCModule INSTANCE;

    Config<String> ipConfig = new StringConfig.Builder("IP")
            .setDefaultValue("127.0.0.1").build();
    Config<String> portConfig = new StringConfig.Builder("Port")
            .setDefaultValue("25565").build();

    public HeadlessMCModule()
    {
        super("HeadlessMC", "Allows you to connect to a HeadlessMC instance", GuiCategory.CLIENT);
        INSTANCE = this;
        unregisterConfig(keybind);
    }
}
