package net.shoreline.client.impl.irc.packet.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.ServerPacket;

public final class SPacketServerMessage extends ServerPacket
{
    private final String message;

    public static SPacketServerMessage newInstance(String packet)
    {
        SPacketServerMessage packetServerMessage;
        try
        {
            packetServerMessage = new SPacketServerMessage(packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetServerMessage;
    }

    private SPacketServerMessage(String packet) throws Throwable
    {
        JsonObject object = JsonParser.parseString(packet).getAsJsonObject();

        this.message = object.get("Message").getAsString();
    }

    @Override
    public void apply(IRCManager ircManager)
    {
        ircManager.addServerMessageToChat(Formatting.RED + this.message);
    }
}
