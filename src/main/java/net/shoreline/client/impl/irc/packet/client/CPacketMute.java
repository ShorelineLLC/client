package net.shoreline.client.impl.irc.packet.client;

import com.google.gson.JsonObject;
import net.shoreline.client.impl.irc.packet.IRCPacket;
import net.shoreline.client.impl.irc.user.OnlineUser;

public final class CPacketMute extends IRCPacket
{
    private final OnlineUser user;
    private final boolean mute;

    public CPacketMute(OnlineUser user, boolean mute)
    {
        super("CPacketMute");

        this.user = user;
        this.mute = mute;
    }

    @Override
    public void addData(JsonObject object)
    {
        object.addProperty("Target-User", this.user.getName());
        object.addProperty("Muted", this.mute);
    }
}
