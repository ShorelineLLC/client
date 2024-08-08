package net.shoreline.client.impl.irc.packet.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.ServerPacket;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.impl.module.client.CapesModule;

public final class SPacketChatMessage extends ServerPacket
{
    private final String message;
    private final OnlineUser sender;

    public static SPacketChatMessage newInstance(String packet)
    {
        SPacketChatMessage packetChatMessage;
        try
        {
            packetChatMessage = new SPacketChatMessage(packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetChatMessage;
    }

    private SPacketChatMessage(String packet) throws Throwable
    {
        JsonObject object = JsonParser.parseString(packet).getAsJsonObject();

        this.message = object.get("Message").getAsString();

        JsonObject senderObject = object.get("Sender").getAsJsonObject();

        String username = senderObject.get("Username").getAsString();
        String usertype = senderObject.get("User-Type").getAsString();

        OnlineUser.UserType type = switch (usertype.toLowerCase())
        {
            case "release" -> OnlineUser.UserType.RELEASE;
            case "beta" -> OnlineUser.UserType.BETA;
            case "dev" -> OnlineUser.UserType.DEV;
            default -> throw new IllegalStateException("Unrecognized session user type");
        };

        this.sender = new OnlineUser(username, type, CapesModule.Capes.OFF);
    }

    @Override
    public void apply(IRCManager ircManager)
    {
        String message = Formatting.GRAY
                + "<" + this.sender.getUsertype().getColorCode() + this.sender.getName() + Formatting.GRAY + "> "
                + this.sender.getUsertype().getChatColorCode() + this.message;

        ircManager.addToChat(message);
    }
}
