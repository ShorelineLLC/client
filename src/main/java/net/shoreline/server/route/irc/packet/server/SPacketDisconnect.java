package net.shoreline.server.route.irc.packet.server;

import com.google.gson.JsonObject;
import net.shoreline.server.route.irc.packet.IRCPacket;

public final class SPacketDisconnect extends IRCPacket
{
    private final String reason;
    private final boolean fullyKilled;

    public SPacketDisconnect(String reason,
                             boolean fullyKilled)
    {
        super("SPacketDisconnect");

        this.reason = reason;
        this.fullyKilled = fullyKilled;
    }

    @Override
    public void addData(JsonObject obj)
    {
        obj.addProperty("Reason", this.reason);
        obj.addProperty("Fully-Killed", this.fullyKilled);
    }
}
