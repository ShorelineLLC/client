package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;

public class AnticheatModule extends ConcurrentModule
{
    private static AnticheatModule INSTANCE;

    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Applies grim strict directions", false));

    public AnticheatModule()
    {
        super("Anticheat", "Settings for anticheat configs", ModuleCategory.CLIENT);
        INSTANCE = this;
    }

    public static AnticheatModule getInstance()
    {
        return INSTANCE;
    }

    public boolean isGrim()
    {
        return grimConfig.getValue();
    }
}
