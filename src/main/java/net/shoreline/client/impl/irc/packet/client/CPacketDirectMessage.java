package net.shoreline.client.impl.irc.packet.client;

import com.google.gson.JsonObject;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.IRCPacket;
import net.shoreline.client.impl.irc.user.OnlineUser;

public final class CPacketDirectMessage extends IRCPacket
{
    private final OnlineUser onlineUser;
    private final String message;

    public CPacketDirectMessage(OnlineUser onlineUser,
                                String message)
    {
        super("CPacketDirectMessage");

        this.onlineUser = onlineUser;
        this.message = message;
    }

    @Override
    public void addData(JsonObject object)
    {
        object.addProperty("Message", this.message);
        object.addProperty("Target-User", this.onlineUser.getName());
    }

    @Override
    public void onSend(IRCManager ircManager)
    {
        String message = Formatting.ITALIC + "§sTo "
                + this.onlineUser.getUsertype().getColorCode() + Formatting.ITALIC + this.onlineUser.getName()
                + "§s: " + this.message;

        ircManager.addToChat(message);
    }
}
