package net.shoreline.client.impl.irc.packet.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.ServerPacket;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.impl.module.client.CapesModule;

import java.util.ArrayList;
import java.util.List;

public final class SPacketPong extends ServerPacket
{
    private final List<OnlineUser> activeOnlineUsers = new ArrayList<>();
    private final List<OnlineUser> allOnlineUsers = new ArrayList<>();
    private final boolean muted;

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

        JsonArray activeUsers = object.get("Active-Online-Users").getAsJsonArray();

        for (JsonElement user : activeUsers.asList())
        {
            JsonObject session = user.getAsJsonObject();

            String userName = session.get("Username").getAsString();
            String userType = session.get("User-Type").getAsString();
            String capeColor = session.get("Cape-Color").getAsString();

            OnlineUser.UserType type = switch (userType.toLowerCase())
            {
                case "release" -> OnlineUser.UserType.RELEASE;
                case "beta" -> OnlineUser.UserType.BETA;
                case "dev" -> OnlineUser.UserType.DEV;
                default -> throw new IllegalStateException("Unrecognized session user type" + userType);
            };

            CapesModule.Capes capes = switch (capeColor.toLowerCase())
            {
                case "black" -> CapesModule.Capes.BLACK;
                case "white" -> CapesModule.Capes.WHITE;
                case "off" -> CapesModule.Capes.OFF;
                default -> throw new IllegalStateException("Unrecognized cape color" + capeColor);
            };

            OnlineUser onlineUser = new OnlineUser(userName, type, capes);
            this.activeOnlineUsers.add(onlineUser);
        }

        JsonArray users = object.get("All-Online-Users").getAsJsonArray();

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
                default -> throw new IllegalStateException("Unrecognized session user type" + userType);
            };

            OnlineUser onlineUser = new OnlineUser(userName, type, CapesModule.Capes.OFF);
            this.allOnlineUsers.add(onlineUser);
        }

        this.muted = object.get("Muted").getAsBoolean();
    }

    @Override
    public void apply(IRCManager ircManager)
    {
        ircManager.getActiveOnlineUsers().clear();
        ircManager.getActiveOnlineUsers().addAll(this.activeOnlineUsers);

        ircManager.getAllOnlineUsers().clear();
        ircManager.getAllOnlineUsers().addAll(this.allOnlineUsers);

        ircManager.MUTED = this.muted;
    }
}
