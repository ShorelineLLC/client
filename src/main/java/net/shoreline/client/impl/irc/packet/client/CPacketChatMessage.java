package net.shoreline.client.impl.irc.packet.client;

import com.google.gson.JsonObject;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.IRCPacket;
import net.shoreline.loader.Loader;

public final class CPacketChatMessage extends IRCPacket
{
    private final String message;

    public CPacketChatMessage(String message)
    {
        super("CPacketChatMessage");

        this.message = message;
    }

    @Override
    public void addData(JsonObject data)
    {
        data.addProperty("Message", this.message);
    }

    @Override
    public void onSend(IRCManager ircManager)
    {
        Formatting colorCode = switch (Loader.SESSION.getUserType())
        {
            case "dev" -> Formatting.RED;
            case "beta" -> Formatting.BLUE;
            case "release" -> Formatting.GRAY;
            default -> throw new IllegalStateException("unknown type");
        };

        String message = colorCode
                + "<" + Loader.SESSION.getUsername() + "> " + Formatting.GRAY + this.message;

        if (ircManager.MUTED)
        {
            return;
        }

        ircManager.addToChat(message);
    }
}
