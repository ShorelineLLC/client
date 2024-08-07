package net.shoreline.loader.session;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.shoreline.loader.Loader;

import java.util.List;

public final class UserSession
{
    private final String hardwareId;
    private final String username;
    private final String uid;
    private final String usertype;
    private final List<String> runningMods;

    public UserSession(String hardwareId,
                       String username,
                       String uid,
                       String usertype,
                       List<String> runningMods)
    {
        this.hardwareId = hardwareId;
        this.username = username;
        this.uid = uid;
        this.usertype = usertype;
        this.runningMods = runningMods;
    }

    public String getHardwareID()
    {
        return this.hardwareId;
    }

    public String getUsername()
    {
        return this.username;
    }

    public String getUID()
    {
        return this.uid;
    }

    public String getUserType()
    {
        return this.usertype;
    }

    public List<String> getRunningMods()
    {
        return this.runningMods;
    }

    public static UserSession load()
    {
        Object userInfo = getUserInfo("unused_obscure");

        String hardwareID, username, uid, usertype;
        try
        {
            JsonObject info = JsonParser.parseString(userInfo.toString()).getAsJsonObject();

            hardwareID = info.get("Hardware-ID").getAsString();
            username = info.get("Username").getAsString();
            uid = info.get("UID").getAsString();
            usertype = info.get("User-Type").getAsString();
        } catch (Throwable t)
        {
            Loader.error("Failed to parse your user info. Please report this to a developer.");
            Loader.showErrorWindow("Failed to parse your user info. Please report this to a developer.");
            System.exit(-1);
            return null;
        }

        List<String> loadedMods = FabricLoader.getInstance().getAllMods()
                .stream()
                .map(mod -> mod.getMetadata().getName())
                .filter(mod -> !mod.contains("Fabric"))
                .toList();

        return new UserSession(
                hardwareID,
                username,
                uid,
                usertype,
                loadedMods
        );
    }

    private static native Object getUserInfo(Object unused);
}
