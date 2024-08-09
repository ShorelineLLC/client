package net.shoreline.server.route.irc.packet;

import com.google.gson.JsonObject;

public abstract class IRCPacket
{
    private final String name;

    public IRCPacket(String name)
    {
        this.name = name;
    }

    public abstract void addData(JsonObject obj);

    public final String fullySerialize()
    {
        JsonObject object = new JsonObject();
        object.addProperty("Packet", this.name);

        addData(object);

        return object.toString();
    }
}