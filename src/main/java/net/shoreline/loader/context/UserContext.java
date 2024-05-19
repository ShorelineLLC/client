package net.shoreline.loader.context;

public final class UserContext
{
    private String hwid;
    private String username;
    private String uid;
    private UserType userType;

    public UserContext(String hwid,
                       String username,
                       String uid,
                       UserType userType)
    {
        this.hwid = hwid;
        this.username = username;
        this.uid = uid;
        this.userType = userType;
    }

    public String hwid()
    {
        return this.hwid;
    }

    public String username()
    {
        return this.username;
    }

    public String uid()
    {
        return this.uid;
    }

    public UserType userType()
    {
        return userType;
    }

    public UserContext hwid(String hwid)
    {
        if (this.hwid != null)
        {
            throw new IllegalStateException("Field already set");
        }

        this.hwid = hwid;

        return this;
    }

    public UserContext username(String username)
    {
        if (this.username != null)
        {
            throw new IllegalStateException("Field already set");
        }

        this.username = username;

        return this;
    }

    public UserContext uid(String uid)
    {
        if (this.uid != null)
        {
            throw new IllegalStateException("Field already set");
        }

        this.uid = uid;

        return this;
    }

    public UserContext userType(UserType userType)
    {
        if (this.userType != null)
        {
            throw new IllegalStateException("Field already set");
        }

        this.userType = userType;

        return this;
    }

    public boolean isNone()
    {
        return this.hwid == null && this.username == null && this.uid == null && this.username == null;
    }

    public static UserContext none()
    {
        return new UserContext(null, null, null, UserType.USER);
    }
}
