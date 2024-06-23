package net.shoreline.loader.impl.context;

import net.shoreline.loader.Natives;

import java.util.List;

public final class UserContext
{
    private String hwid;
    private String username;
    private String uid;
    private String usertype;
    private List<String> runningMods;

    public UserContext(String hwid,
                       String username,
                       String uid,
                       String usertype,
                       List<String> runningMods)
    {
        this.hwid = hwid;
        this.username = username;
        this.uid = uid;
        this.usertype = usertype;
        this.runningMods = runningMods;
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

    public String usertype()
    {
        return this.usertype;
    }

    public List<String> runningMods()
    {
        return this.runningMods;
    }

    public UserContext setHwid(String hwid)
    {
        if (this.hwid != null)
        {
            throw new IllegalStateException("Field already set");
        }

        this.hwid = hwid;

        return this;
    }

    public UserContext setUsername(String username)
    {
        if (this.username != null)
        {
            throw new IllegalStateException("Field already set");
        }

        this.username = username;

        return this;
    }

    public UserContext setUid(String uid)
    {
        if (this.uid != null)
        {
            throw new IllegalStateException("Field already set");
        }

        this.uid = uid;

        return this;
    }

    public UserContext setUserType(String usertype)
    {
        if (this.usertype != null)
        {
            throw new IllegalStateException("Field already set");
        }

        this.usertype = usertype;

        return this;
    }

    public void setRunningMods(List<String> runningMods)
    {
        if (this.runningMods != null)
        {
            throw new IllegalStateException("Field already set");
        }

        this.runningMods = runningMods;
    }

    public boolean isNone()
    {
        return this.hwid == null && this.username == null && this.uid == null && this.usertype == null && this.runningMods == null;
    }

    /**
     * Use the information in this user context to alert the webhook
     */
    public void alert(String message)
    {
        String[] information = getInformationArray();
        information[0] = message;
        Natives.h(information);
    }

    public String[] getInformationArray()
    {
        StringBuilder builder = new StringBuilder();

        for (String mod : runningMods())
        {
            builder.append(mod).append(", ");
        }

        String modList = builder.toString();

        return new String[] {
                null, // Message
                hwid(),
                username(),
                usertype(),
                modList.substring(0, modList.length() - 2)
        };
    }

    public static UserContext none()
    {
        return new UserContext(null, null, null, null, null);
    }
}
