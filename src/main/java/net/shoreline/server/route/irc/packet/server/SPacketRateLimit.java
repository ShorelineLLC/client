package net.shoreline.server.route.irc.packet.server;

import com.google.gson.JsonObject;
import net.shoreline.server.route.irc.packet.IRCPacket;

public final class SPacketRateLimit extends IRCPacket
{
    public SPacketRateLimit()
    {
        super("SPacketRateLimit");
    }


    @Override
    public void addData(JsonObject obj)
    {
    }
}
