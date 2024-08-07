package net.shoreline.client.impl.irc.packet.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.ServerPacket;
import net.shoreline.loader.Loader;

import java.util.ArrayList;
import java.util.List;

public final class SPacketPong extends ServerPacket
{
    private final List<String> activeOnlineUsers = new ArrayList<>();

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
            String userString = user.toString();
            this.activeOnlineUsers.add(userString);
        }
    }

    @Override
    public void apply(IRCManager ircManager)
    {
        ircManager.getActiveOnlineUsers().clear();
        ircManager.getActiveOnlineUsers().addAll(this.activeOnlineUsers);
    }
}
