package net.shoreline.client.impl.irc.packet.client;

import com.google.gson.JsonObject;
import net.shoreline.client.impl.irc.packet.IRCPacket;

public final class CPacketDirectServerMsg extends IRCPacket
{
    private final String username;
    private final String message;

    public CPacketDirectServerMsg(String username,
                                  String message)
    {
        super("CPacketDirectServerMsg");

        this.username = username;
        this.message = message;
    }

    @Override
    public void addData(JsonObject object)
    {
        object.addProperty("Message", this.message);
        object.addProperty("Target-User", this.username);
    }
}
