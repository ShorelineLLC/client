package net.shoreline.client.impl.irc.packet;

import com.google.gson.JsonObject;
import net.shoreline.client.impl.irc.IRCManager;

public abstract class IRCPacket
{
    private final String name;

    public IRCPacket(String name)
    {
        this.name = name;
    }

    public abstract void addData(JsonObject object);

    public void onSend(IRCManager ircManager) {}

    public final String fullySerialize()
    {
        JsonObject object = new JsonObject();

        object.addProperty("Packet", this.name);
        addData(object);

        return object.toString();
    }
}
