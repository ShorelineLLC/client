package net.shoreline.client.irc.packet.client;

import com.google.gson.JsonObject;
import net.shoreline.client.impl.module.client.CapesModule;
import net.shoreline.client.impl.module.client.IRCModule;
import net.shoreline.client.irc.packet.IRCPacket;

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
    public void addData(JsonObject data)
    {
        data.addProperty("Session-Username", currentSessionUsername);
        data.addProperty("Connected-Server", currentConnectedServer);
        data.addProperty("Cape-Color", CapesModule.INSTANCE.getCapeType().getValue().name());
        data.addProperty("Chat-Enabled", IRCModule.INSTANCE.isEnabled());
    }
}
