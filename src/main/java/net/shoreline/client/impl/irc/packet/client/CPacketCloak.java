package net.shoreline.client.impl.irc.packet.client;

import com.google.gson.JsonObject;
import net.shoreline.client.impl.irc.packet.IRCPacket;

public final class CPacketCloak extends IRCPacket
{
    private final String cloakType;

    public CPacketCloak(String cloakType)
    {
        super("CPacketCloak");

        this.cloakType = cloakType;
    }

    @Override
    public void addData(JsonObject data)
    {
        data.addProperty("Cloak-Type", this.cloakType);
    }
}
