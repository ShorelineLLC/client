package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;

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
    Config<Color> enemyColor = new ColorConfig.Builder("EnemiesColor")
            .setRgb(0xffff192d)
            .setDescription("The color for enemies in renders")
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

    public Color getEnemiesColor()
    {
        return enemyColor.getValue();
    }

    public Color getEntityColor(Entity entity, Color fallback)
    {
        return getEntityColor(entity.getName().getString(), fallback);
    }

    public Color getEntityColor(String name, Color fallback)
    {
        if (Managers.SOCIAL.isFriend(name))
        {
            return friendsColor.getValue();
        }
        else if (Managers.SOCIAL.isEnemy(name))
        {
            return enemyColor.getValue();
        }

        return fallback;
    }
}
