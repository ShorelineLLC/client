package net.shoreline.client.impl.irc.packet.client;

import com.google.gson.JsonObject;
import net.shoreline.client.impl.irc.packet.IRCPacket;

public final class CPacketBroadcastServerMsg extends IRCPacket
{
    private final String message;

    public CPacketBroadcastServerMsg(String message)
    {
        super("CPacketBroadcastServerMsg");

        this.message = message;
    }

    @Override
    public void addData(JsonObject data)
    {
        data.addProperty("Message", this.message);
    }
}
