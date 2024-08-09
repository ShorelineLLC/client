package net.shoreline.server.route.irc.packet.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.server.route.irc.packet.ClientPacket;
import net.shoreline.server.route.irc.packet.IRCPacket;
import net.shoreline.server.route.irc.server.IRCServer;
import net.shoreline.server.route.loader.LoaderEndpoint;

public final class CPacketDirectServerMsg extends ClientPacket
{
    private final LoaderEndpoint.IRCSession session;
    private final String message;
    private final String targetUser;

    public static CPacketDirectServerMsg newInstance(LoaderEndpoint.IRCSession session,
                                                     String packet)
    {
        CPacketDirectServerMsg packetDirectServerMsg;
        try
        {
            packetDirectServerMsg = new CPacketDirectServerMsg(session, packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetDirectServerMsg;
    }

    public CPacketDirectServerMsg(LoaderEndpoint.IRCSession session,
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
        if (!this.session.getUsertype().equals("dev"))
        {
            server.sendServerMessage("You do not have permission to execute this command.", this.session);
            return;
        }

        LoaderEndpoint.IRCSession ircSession = server.getSessionByUsername(this.targetUser);

        if (ircSession == null)
        {
            server.sendServerMessage("No one by the name of " + this.targetUser + " is online.", this.session);
            return;
        }

        server.sendServerMessage(this.message, ircSession);
    }

    @Override
    public IRCPacket getResponse(IRCServer server)
    {
        return null;
    }
}
