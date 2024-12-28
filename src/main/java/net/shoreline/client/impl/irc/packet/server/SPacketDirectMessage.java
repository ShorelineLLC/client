package net.shoreline.client.impl.irc.packet.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.ServerPacket;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.impl.manager.world.sound.SoundManager;
import net.shoreline.client.impl.module.client.CapesModule;
import net.shoreline.client.impl.module.client.ChatModule;
import net.shoreline.client.impl.module.misc.PMSoundModule;
import net.shoreline.client.init.Managers;

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
        if (!ChatModule.getInstance().isEnabled())
        {
            return;
        }

        String message = Formatting.ITALIC + "§7From " + this.sender.getUsertype().getColorCode()
                + this.sender.getName() + Formatting.ITALIC + "§7: " + this.message;
        ircManager.addToChat(message);

        ircManager.setLastMessageReceivedUser(sender);

        if (PMSoundModule.getInstance().isEnabled())
        {
            Managers.SOUND.playSound(switch (PMSoundModule.getInstance().getPMSound())
            {
                case TWITTER -> SoundManager.TWITTER;
                case IOS -> SoundManager.IOS;
                case DISCORD -> SoundManager.DISCORD;
                case STEAM -> SoundManager.STEAM;
            });
        }
    }
}
