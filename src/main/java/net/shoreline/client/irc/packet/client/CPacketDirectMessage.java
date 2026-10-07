package net.shoreline.client.irc.packet.client;

import com.google.gson.JsonObject;
import net.minecraft.util.Formatting;
import net.shoreline.client.irc.IRCManager;
import net.shoreline.client.irc.packet.IRCPacket;
import net.shoreline.client.irc.user.OnlineUser;

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
        String message = Formatting.ITALIC + "§7To " + this.onlineUser.getUsertype().getColorCode()
                + this.onlineUser.getName() + Formatting.ITALIC + "§7: " + this.message;

        ircManager.addToChat(message);
        ircManager.setLastMessagedUser(this.onlineUser);
    }
}
