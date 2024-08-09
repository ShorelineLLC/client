package net.shoreline.server.route.irc.packet.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.server.route.irc.packet.ClientPacket;
import net.shoreline.server.route.irc.packet.IRCPacket;
import net.shoreline.server.route.irc.server.IRCServer;
import net.shoreline.server.route.loader.LoaderEndpoint;

public final class CPacketChatMessage extends ClientPacket
{
    private final LoaderEndpoint.IRCSession session;
    private final String message;

    public static CPacketChatMessage newInstance(LoaderEndpoint.IRCSession session,
                                                 String packet)
    {
        CPacketChatMessage packetPing;
        try
        {
            packetPing = new CPacketChatMessage(session, packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetPing;
    }

    public CPacketChatMessage(LoaderEndpoint.IRCSession session,
                              String packet) throws Throwable
    {
        this.session = session;

        JsonObject object = JsonParser.parseString(packet).getAsJsonObject();

        this.message = object.get("Message").getAsString();
    }

    @Override
    public void apply(IRCServer server)
    {
        if (server.isMuted(this.session))
        {
            server.sendServerMessage("You are muted.", this.session);
            return;
        }

        server.broadcastUserMessage(this.message, this.session);
    }

    @Override
    public IRCPacket getResponse(IRCServer server)
    {
        return null;
    }
}
