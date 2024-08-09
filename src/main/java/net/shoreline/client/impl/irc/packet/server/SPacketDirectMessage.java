package net.shoreline.client.impl.irc.packet.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.ServerPacket;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.impl.module.client.CapesModule;

public final class SPacketDirectMessage extends ServerPacket
{
    private final String message;
    private final OnlineUser sender;

    public static SPacketDirectMessage newInstance(String packet)
    {
        SPacketDirectMessage packetDirectMessage;
        try
        {
            packetDirectMessage = new SPacketDirectMessage(packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetDirectMessage;
    }

    private SPacketDirectMessage(String packet) throws Throwable
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
        String message = Formatting.ITALIC + "§sFrom "
                + this.sender.getUsertype().getColorCode() + Formatting.ITALIC + this.sender.getName()
                + "§s: " + this.message;

        ircManager.addToChat(message);
    }
}
