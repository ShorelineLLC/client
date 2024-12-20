package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.ClientColorEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class SocialsModule extends ConcurrentModule
{
    private static SocialsModule INSTANCE;

    Config<Boolean> friendsConfig = register(new BooleanConfig("Friends", "Allows friend system to function", true));
    Config<Boolean> addNotifyConfig = register(new BooleanConfig("AddNotify", "Notifies players when you add them as a friend", false, () -> friendsConfig.getValue()));
    Config<Color> friendsColorConfig = register(new ColorConfig("FriendsColor", "The color for friends in the client", new Color(0xff66ffff), false, false, () -> friendsConfig.getValue()));

    public SocialsModule()
    {
        super("Socials", "The client socials system", ModuleCategory.CLIENT);
        INSTANCE = this;
    }

    public static SocialsModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onClientFriendColor(ClientColorEvent.Friend event)
    {
        event.setRgb(getFriendRGB());
    }

    public boolean isFriendsEnabled()
    {
        return friendsConfig.getValue();
    }

    public boolean shouldNotify()
    {
        return addNotifyConfig.getValue();
    }

    public Color getFriendColor()
    {
        return friendsColorConfig.getValue();
    }

    public int getFriendRGB()
    {
        return getFriendColor().getRGB();
    }
}
