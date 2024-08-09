package net.shoreline.server.route.irc.packet.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.server.route.irc.packet.ClientPacket;
import net.shoreline.server.route.irc.packet.IRCPacket;
import net.shoreline.server.route.irc.server.IRCServer;
import net.shoreline.server.route.loader.LoaderEndpoint;

public final class CPacketDirectMessage extends ClientPacket
{
    private final LoaderEndpoint.IRCSession session;
    private final String message;
    private final String targetUser;

    public static CPacketDirectMessage newInstance(LoaderEndpoint.IRCSession session,
                                                   String packet)
    {
        CPacketDirectMessage packetDirectMessage;
        try
        {
            packetDirectMessage = new CPacketDirectMessage(session, packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetDirectMessage;
    }

    public CPacketDirectMessage(LoaderEndpoint.IRCSession session,
                                String packet) throws Throwable
    {
        this.session = session;

        JsonObject object = JsonParser.parseString(packet).getAsJsonObject();

        this.message = object.get("Message").getAsString();
        this.targetUser = object.get("Target-User").getAsString();
    }

    @Override
    public void apply(IRCServer server)
    {
        LoaderEndpoint.IRCSession ircSession = server.getSessionByUsername(this.targetUser);

        if (ircSession == null)
        {
            server.sendServerMessage("No one by the name of " + this.targetUser + " is online.", this.session);
            return;
        }

        if (!ircSession.isChatEnabled())
        {
            server.sendServerMessage("The target user has their IRC chat disabled.", this.session);
            return;
        }

        server.sendPrivateMessage(this.message, ircSession, this.session);
    }

    @Override
    public IRCPacket getResponse(IRCServer server)
    {
        return null;
    }
}
