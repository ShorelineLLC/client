package net.shoreline.client.impl.irc.packet.client;

import com.google.gson.JsonObject;
import net.shoreline.client.impl.irc.packet.IRCPacket;

public final class CPacketPing extends IRCPacket
{
    private final String currentSessionUsername;
    private final String currentConnectedServer;

    public CPacketPing(String currentSessionUsername,
                       String currentConnectedServer)
    {
        super("CPacketPing");
        this.currentSessionUsername = currentSessionUsername;
        this.currentConnectedServer = currentConnectedServer;
    }

    @Override
    public JsonObject asJsonObject()
    {
        JsonObject object = new JsonObject();

        object.addProperty("Session-Username", currentSessionUsername);
        object.addProperty("Connected-Server", currentConnectedServer);

        return object;
    }
}
