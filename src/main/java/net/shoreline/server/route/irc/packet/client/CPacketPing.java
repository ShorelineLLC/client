package net.shoreline.server.route.irc.packet.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.server.route.irc.packet.ClientPacket;
import net.shoreline.server.route.irc.packet.IRCPacket;
import net.shoreline.server.route.irc.packet.server.SPacketPong;
import net.shoreline.server.route.irc.server.IRCServer;
import net.shoreline.server.route.loader.LoaderEndpoint;

public final class CPacketPing extends ClientPacket
{
    private final String sessionId;
    private final LoaderEndpoint.IRCSession session;
    private final String currentUserName;
    private final String currentServer;
    private final String capeColor;

    public static CPacketPing newInstance(String sessionId,
                                          LoaderEndpoint.IRCSession session,
                                          String packet)
    {
        CPacketPing packetPing;
        try
        {
            packetPing = new CPacketPing(sessionId, session, packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetPing;
    }

    private CPacketPing(String sessionId,
                        LoaderEndpoint.IRCSession session,
                        String packet) throws Throwable
    {
        this.sessionId = sessionId;
        this.session = session;

        JsonObject object = JsonParser.parseString(packet).getAsJsonObject();

        this.currentUserName = object.get("Session-Username").getAsString();
        this.currentServer = object.get("Connected-Server").getAsString();
        this.capeColor = object.get("Cape-Color").getAsString();
        boolean chatEnabled = object.get("Chat-Enabled").getAsBoolean();

        session.setChatEnabled(chatEnabled);
    }

    @Override
    public void apply(IRCServer server)
    {
        if (!this.currentServer.equals("null"))
        {
            OnlineUser user = new OnlineUser(this.session, this.currentUserName, this.currentServer, this.capeColor);
            server.updateOnlineUser(this.sessionId, user);
        }
    }

    @Override
    public IRCPacket getResponse(IRCServer server)
    {
        OnlineUser user = new OnlineUser(this.session, this.currentUserName, this.currentServer, this.capeColor);
        return new SPacketPong(server, this.currentServer, user);
    }

    public record OnlineUser(LoaderEndpoint.IRCSession currentSession,
                             String currentUserName,
                             String currentServer,
                             String capeColor) { }
}
