package net.shoreline.client.impl.irc.user;

import net.minecraft.util.Formatting;
import net.shoreline.client.impl.module.client.CapesModule;

public final class OnlineUser
{
    private final String name;
    private final UserType usertype;
    private final CapesModule.Capes capeColor;

    public OnlineUser(String name,
                      UserType usertype,
                      CapesModule.Capes capeColor)
    {
        this.name = name;
        this.usertype = usertype;
        this.capeColor = capeColor;
    }

    public String getName()
    {
        return this.name;
    }

    public UserType getUsertype()
    {
        return this.usertype;
    }

    public CapesModule.Capes getCapeColor()
    {
        return this.capeColor;
    }

    public enum UserType
    {
        RELEASE(Formatting.WHITE),
        BETA(Formatting.BLUE),
        DEV(Formatting.RED);

        private final Formatting colorCode;

        UserType(Formatting colorCode)
        {
            this.colorCode = colorCode;
        }

        public Formatting getColorCode()
        {
            return this.colorCode;
        }
    }
}
