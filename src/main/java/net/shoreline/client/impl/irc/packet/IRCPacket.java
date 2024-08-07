package net.shoreline.client.impl.irc.packet;

import com.google.gson.JsonObject;

public abstract class IRCPacket
{
    private final String name;

    public IRCPacket(String name)
    {
        this.name = name;
    }

    public abstract JsonObject asJsonObject();

    public final String fullySerialize()
    {
        JsonObject object = asJsonObject();

        object.addProperty("Packet", this.name);

        return object.toString();
    }
}
