package net.shoreline.client.impl.irc.packet.client;

import com.google.gson.JsonObject;
import net.shoreline.client.impl.irc.packet.IRCPacket;

public final class CPacketChatMessage extends IRCPacket
{
    private final String message;

    public CPacketChatMessage(String message)
    {
        super("CPacketChatMessage");

        this.message = message;
    }

    @Override
    public JsonObject asJsonObject()
    {
        JsonObject obj = new JsonObject();

        obj.addProperty("Message", this.message);

        return obj;
    }
}
