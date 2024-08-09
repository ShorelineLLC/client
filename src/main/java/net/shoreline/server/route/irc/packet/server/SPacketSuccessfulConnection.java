package net.shoreline.server.route.irc.packet.server;

import com.google.gson.JsonObject;
import net.shoreline.server.route.irc.packet.IRCPacket;

public final class SPacketSuccessfulConnection extends IRCPacket
{
    private final String backupToken;

    public SPacketSuccessfulConnection(String backupToken)
    {
        super("SPacketSuccessfulConnection");

        this.backupToken = backupToken;
    }

    @Override
    public void addData(JsonObject obj)
    {
        obj.addProperty("Reconnection-Token", this.backupToken);
    }
}
