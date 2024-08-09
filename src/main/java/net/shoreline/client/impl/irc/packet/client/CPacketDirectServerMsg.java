package net.shoreline.client.impl.irc.packet.client;

import com.google.gson.JsonObject;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.IRCPacket;
import net.shoreline.client.impl.irc.user.OnlineUser;

public final class CPacketDirectServerMsg extends IRCPacket
{
    private final OnlineUser user;
    private final String message;

    public CPacketDirectServerMsg(OnlineUser user,
                                  String message)
    {
        super("CPacketDirectServerMsg");

        this.user = user;
        this.message = message;
    }

    @Override
    public void addData(JsonObject object)
    {
        object.addProperty("Message", this.message);
        object.addProperty("Target-User", this.user.getName());
    }

    @Override
    public void onSend(IRCManager ircManager)
    {
        String message = "Directed a server message to "
                + this.user.getUsertype().getColorCode() + this.user.getName()
                + Formatting.WHITE + ".";

        ircManager.addServerMessageToChat(message);
    }
}
