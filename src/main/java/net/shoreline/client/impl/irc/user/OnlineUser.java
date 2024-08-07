package net.shoreline.client.impl.irc.user;

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
        RELEASE,
        BETA,
        DEV
    }
}
