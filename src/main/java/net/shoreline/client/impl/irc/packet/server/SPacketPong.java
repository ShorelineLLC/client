package net.shoreline.client.impl.irc.packet.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.ServerPacket;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.loader.Loader;

import java.util.ArrayList;
import java.util.List;

public final class SPacketPong extends ServerPacket
{
    private final List<OnlineUser> activeOnlineUsers = new ArrayList<>();

    public static SPacketPong newInstance(String packet)
    {
        SPacketPong packetPong;
        try
        {
            packetPong = new SPacketPong(packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetPong;
    }

    /**
     * Will exception if the packet string is malformed
     */
    private SPacketPong(String packet) throws Throwable
    {
        JsonObject object = JsonParser.parseString(packet).getAsJsonObject();

        JsonArray users = object.get("Online-Users").getAsJsonArray();

        for (JsonElement user : users.asList())
        {
            JsonObject session = user.getAsJsonObject();

            String userName = session.get("Username").getAsString();
            String userType = session.get("User-Type").getAsString();

            OnlineUser.UserType type = switch (userType.toLowerCase())
            {
                case "release" -> OnlineUser.UserType.RELEASE;
                case "beta" -> OnlineUser.UserType.BETA;
                case "dev" -> OnlineUser.UserType.DEV;
                default -> throw new IllegalStateException("Unrecognized session user type");
            };

            OnlineUser onlineUser = new OnlineUser(userName, type);
            this.activeOnlineUsers.add(onlineUser);
        }
    }

    @Override
    public void apply(IRCManager ircManager)
    {
        ircManager.getActiveOnlineUsers().clear();
        ircManager.getActiveOnlineUsers().addAll(this.activeOnlineUsers);
    }
}
