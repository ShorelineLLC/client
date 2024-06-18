package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;

public class SocialsModule extends ConcurrentModule
{
    private static SocialsModule INSTANCE;

    Config<Boolean> friendsConfig = register(new BooleanConfig("Friends", "Allows friend system to function", true));

    public SocialsModule()
    {
        super("Socials", "The client socials system", ModuleCategory.CLIENT);
        INSTANCE = this;
    }

    public static SocialsModule getInstance()
    {
        return INSTANCE;
    }

    public boolean isFriendsEnabled()
    {
        return friendsConfig.getValue();
    }
}
