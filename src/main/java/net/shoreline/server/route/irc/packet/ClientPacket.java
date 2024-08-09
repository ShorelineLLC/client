package net.shoreline.server.route.irc.packet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.server.route.irc.packet.client.*;
import net.shoreline.server.route.irc.server.IRCServer;
import net.shoreline.server.route.loader.LoaderEndpoint;

public abstract class ClientPacket
{
    public abstract void apply(IRCServer server);

    public abstract IRCPacket getResponse(IRCServer server);

    public static ClientPacket deserializeClientPacket(String sessionId,
                                                       LoaderEndpoint.IRCSession session,
                                                       String text)
    {
        JsonObject object;
        try
        {
            object = JsonParser.parseString(text).getAsJsonObject();
        } catch (Throwable t)
        {
            return null;
        }

        JsonElement packet = object.get("Packet");

        if (packet == null)
        {
            return null;
        }

        String name = packet.getAsString();

        return switch (name) {
            case "CPacketPing" -> CPacketPing.newInstance(sessionId, session, text);
            case "CPacketChatMessage" -> CPacketChatMessage.newInstance(session, text);
            case "CPacketCloak" -> CPacketCloak.newInstance(session, text);
            case "CPacketBroadcastServerMsg" -> CPacketBroadcastServerMsg.newInstance(session, text);
            case "CPacketDirectServerMsg" -> CPacketDirectServerMsg.newInstance(session, text);
            case "CPacketDirectMessage" -> CPacketDirectMessage.newInstance(session, text);
            case "CPacketMute" -> CPacketMute.newInstance(session, text);
            default -> null;
        };

    }
}
