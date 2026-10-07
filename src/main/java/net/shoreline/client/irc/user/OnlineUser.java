package net.shoreline.client.irc.user;

import lombok.Getter;
import net.minecraft.util.Formatting;
import net.shoreline.client.irc.CapeType;

@Getter
public final class OnlineUser
{
    private final String name;
    private final UserType usertype;
    private final CapeType capeType;

    public OnlineUser(String name,
                      UserType usertype,
                      CapeType capeType)
    {
        this.name = name;
        this.usertype = usertype;
        this.capeType = capeType;
    }

    @Getter
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
    }
}
