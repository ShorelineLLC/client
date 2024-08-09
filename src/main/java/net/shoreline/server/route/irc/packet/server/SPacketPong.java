package net.shoreline.server.route.irc.packet.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.shoreline.server.route.irc.packet.IRCPacket;
import net.shoreline.server.route.irc.packet.client.CPacketPing;
import net.shoreline.server.route.irc.server.IRCServer;

import java.util.List;

public final class SPacketPong extends IRCPacket
{
    private final List<CPacketPing.OnlineUser> onlinePlayersOnServer;
    private final List<CPacketPing.OnlineUser> allOnlinePlayers;
    private final boolean muted;

    public SPacketPong(IRCServer server,
                       String connectedServer,
                       CPacketPing.OnlineUser user)
    {
        super("SPacketPong");

        if (connectedServer.equals("null"))
        {
            this.onlinePlayersOnServer = List.of(user);
        } else
        {
            this.onlinePlayersOnServer = server.getUsersConnectedTo(connectedServer);
        }

        this.allOnlinePlayers = server.getAllUsers();

        this.muted = server.isMuted(user.currentSession());
    }

    @Override
    public void addData(JsonObject obj)
    {
        JsonArray onlineUsers = new JsonArray();

        for (CPacketPing.OnlineUser user : this.onlinePlayersOnServer)
        {
            JsonObject session = new JsonObject();

            session.addProperty("Username", user.currentUserName());
            session.addProperty("User-Type", user.currentSession().getMaskedUserType());
            session.addProperty("Cape-Color", user.capeColor());

            onlineUsers.add(session);
        }

        obj.add("Active-Online-Users", onlineUsers);

        JsonArray allOnlineUsers = new JsonArray();

        for (CPacketPing.OnlineUser user : this.allOnlinePlayers)
        {
            JsonObject session = new JsonObject();

            session.addProperty("Username", user.currentSession().getUsername());
            session.addProperty("User-Type", user.currentSession().getUsertype());

            allOnlineUsers.add(session);
        }

        obj.add("All-Online-Users", allOnlineUsers);

        obj.addProperty("Muted", this.muted);
    }
}
