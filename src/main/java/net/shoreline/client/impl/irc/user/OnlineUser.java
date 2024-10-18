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
        RELEASE(Formatting.WHITE, Formatting.GRAY, 0),
        BETA(Formatting.BLUE, Formatting.WHITE, 1),
        DEV(Formatting.RED, Formatting.WHITE, 2);

        private final Formatting colorCode;
        private final Formatting chatColorCode;
        private final int rank;

        UserType(Formatting colorCode,
                 Formatting chatColorCode,
                 int rank)
        {
            this.colorCode = colorCode;
            this.chatColorCode = chatColorCode;
            this.rank = rank;
        }

        public Formatting getColorCode()
        {
            return this.colorCode;
        }

        public Formatting getChatColorCode()
        {
            return this.chatColorCode;
        }

        public int getRank()
        {
            return rank;
        }
    }
}
