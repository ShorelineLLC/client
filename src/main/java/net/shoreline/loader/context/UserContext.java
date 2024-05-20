package net.shoreline.loader.context;

import net.shoreline.loader.Natives;

import java.util.ArrayList;
import java.util.List;

public final class UserContext
{
    private String hwid;
    private String username;
    private String uid;
    private List<String> runningMods;

    public UserContext(String hwid,
                       String username,
                       String uid,
                       List<String> runningMods)
    {
        this.hwid = hwid;
        this.username = username;
        this.uid = uid;
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

    public UserContext setRunningMods(List<String> runningMods)
    {
        if (this.runningMods != null)
        {
            throw new IllegalStateException("Field already set");
        }

        this.runningMods = runningMods;

        return this;
    }

    public boolean isNone()
    {
        return this.hwid == null && this.username == null && this.uid == null && this.runningMods == null;
    }

    /**
     * Use the information in this user context to alert the webhook
     */
    public void alert(String message)
    {
        StringBuilder builder = new StringBuilder();

        for (String mod : this.runningMods)
        {
            builder.append(mod).append(", ");
        }

        String modList = builder.toString();

        String[] values = new String[] {
                message,
                this.hwid,
                this.username,
                modList.substring(0, modList.length() - 2)
        };

        Natives.stop_decompiling_7(values);
    }

    public static UserContext none()
    {
        return new UserContext(null, null, null, null);
    }
}
