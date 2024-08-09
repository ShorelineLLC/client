package net.shoreline.server.route.irc.packet.server;

import com.google.gson.JsonObject;
import net.shoreline.server.route.irc.packet.IRCPacket;

public final class SPacketServerMessage extends IRCPacket
{
    private final String message;

    public SPacketServerMessage(String message)
    {
        super("SPacketServerMessage");

        this.message = message;
    }

    @Override
    public void addData(JsonObject obj)
    {
        obj.addProperty("Message", this.message);
    }
}
