package net.shoreline.server.route.irc.packet.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.server.route.irc.packet.ClientPacket;
import net.shoreline.server.route.irc.packet.IRCPacket;
import net.shoreline.server.route.irc.server.IRCServer;
import net.shoreline.server.route.loader.LoaderEndpoint;

public final class CPacketBroadcastServerMsg extends ClientPacket
{
    private final LoaderEndpoint.IRCSession session;
    private final String message;

    public static CPacketBroadcastServerMsg newInstance(LoaderEndpoint.IRCSession session,
                                                 String packet)
    {
        CPacketBroadcastServerMsg packetBroadcastServerMsg;
        try
        {
            packetBroadcastServerMsg = new CPacketBroadcastServerMsg(session, packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetBroadcastServerMsg;
    }

    public CPacketBroadcastServerMsg(LoaderEndpoint.IRCSession session,
                                     String packet) throws Throwable
    {
        this.session = session;

        JsonObject object = JsonParser.parseString(packet).getAsJsonObject();

        this.message = object.get("Message").getAsString();
    }

    @Override
    public void apply(IRCServer server)
    {
        if (!this.session.getUsertype().equals("dev"))
        {
            server.sendServerMessage("You do not have permission to execute this command.", this.session);
            return;
        }

        server.broadcastServerMessage(this.message, false);
    }

    @Override
    public IRCPacket getResponse(IRCServer server)
    {
        return null;
    }
}
