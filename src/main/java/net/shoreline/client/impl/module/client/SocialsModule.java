package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;

import java.awt.*;

@Getter
public class SocialsModule extends Concurrent
{
    public static SocialsModule INSTANCE;

    Config<Boolean> friendsConfig = new BooleanConfig.Builder("Friends")
            .setDescription("Won't target added friends")
            .setDefaultValue(false).build();
    Config<Color> friendsColor = new ColorConfig.Builder("FriendsColor")
            .setRgb(0xff66ffff)
            .setVisible(() -> friendsConfig.getValue())
            .setDescription("The color for friends in renders")
            .build();

    public SocialsModule()
    {
        super("Socials", "Manages client socials", GuiCategory.CLIENT);
        INSTANCE = this;
    }

    public Color getFriendsColor()
    {
        return friendsColor.getValue();
    }

    public Color getFriendColor(Entity entity, Color fallback)
    {
        return null;
    }
}
