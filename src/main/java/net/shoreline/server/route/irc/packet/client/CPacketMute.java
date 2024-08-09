package net.shoreline.server.route.irc.packet.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.server.route.irc.packet.ClientPacket;
import net.shoreline.server.route.irc.packet.IRCPacket;
import net.shoreline.server.route.irc.server.IRCServer;
import net.shoreline.server.route.loader.LoaderEndpoint;

public final class CPacketMute extends ClientPacket
{
    private final LoaderEndpoint.IRCSession session;
    private final String targetUser;
    private final boolean mute;

    public static CPacketMute newInstance(LoaderEndpoint.IRCSession session,
                                          String packet)
    {
        CPacketMute packetMute;
        try
        {
            packetMute = new CPacketMute(session, packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetMute;
    }

    public CPacketMute(LoaderEndpoint.IRCSession session,
                       String packet) throws Throwable
    {
        this.session = session;

        JsonObject object = JsonParser.parseString(packet).getAsJsonObject();

        this.targetUser = object.get("Target-User").getAsString();
        this.mute = object.get("Muted").getAsBoolean();
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

        boolean appliedMute = server.setMuted(ircSession, this.mute);

        if (this.mute)
        {
            String message;
            if (appliedMute)
            {
                message = "Muted " + ircSession.getUsername() + ".";

                server.broadcastServerMessage(ircSession.getUsername() + " has been muted by " + this.session.getUsername() + ".", true);
                server.sendServerMessage("You have been muted.", ircSession);
            } else
            {
                message = ircSession.getUsername() + " is already muted.";
            }

            server.sendServerMessage(message, this.session);
        } else
        {
            String message;
            if (appliedMute)
            {
                message = "Unmuted " + ircSession.getUsername() + ".";

                server.broadcastServerMessage(ircSession.getUsername() + " has been unmuted by " + this.session.getUsername() + ".", true);
                server.sendServerMessage("You have been unmuted.", ircSession);
            } else
            {
                message = ircSession.getUsername() + " is not muted.";
            }

            server.sendServerMessage(message, this.session);
        }
    }

    @Override
    public IRCPacket getResponse(IRCServer server)
    {
        return null;
    }
}
