package net.shoreline.client.impl.irc.packet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.server.*;

public abstract class ServerPacket
{
    public abstract void apply(IRCManager ircManager);

    public static ServerPacket deserializeServerPacket(String text)
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

        return switch (name)
        {
            case "SPacketPong" -> SPacketPong.newInstance(text);
            case "SPacketDisconnect" -> SPacketDisconnect.newInstance(text);
            case "SPacketSuccessfulConnection" -> SPacketSuccessfulConnection.newInstance(text);
            case "SPacketRateLimit" -> new SPacketRateLimit();
            case "SPacketChatMessage" -> SPacketChatMessage.newInstance(text);
            case "SPacketServerMessage" -> SPacketServerMessage.newInstance(text);
            case "SPacketDirectMessage" -> SPacketDirectMessage.newInstance(text);
            default -> null;
        };

    }
}
