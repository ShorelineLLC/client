package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class SoundsModule extends Toggleable
{
    Config<Boolean> loginSound = new BooleanConfig.Builder("Login")
            .setDescription("Plays a sound when a player logs in")
            .setDefaultValue(false).build();
    Config<Boolean> logoutSound = new BooleanConfig.Builder("Logout")
            .setDescription("Plays a sound when a player logs out")
            .setDefaultValue(false).build();
    Config<Boolean> pmSound = new BooleanConfig.Builder("PM")
            .setDescription("Plays a sound when you receive a message")
            .setDefaultValue(false).build();

    public SoundsModule()
    {
        super("Sounds", "Manage client sounds", GuiCategory.CLIENT);
    }


}
