package net.shoreline.client.impl.irc.user;

import net.minecraft.util.Formatting;

public final class OnlineUser
{
    private final String name;
    private final UserType usertype;

    public OnlineUser(String name,
                      UserType usertype)
    {
        this.name = name;
        this.usertype = usertype;
    }

    public String getName()
    {
        return this.name;
    }

    public UserType getUsertype()
    {
        return this.usertype;
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
