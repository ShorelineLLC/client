package net.shoreline.client.impl.irc.packet.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.ServerPacket;
import net.shoreline.loader.Loader;

public final class SPacketDisconnect extends ServerPacket
{
    private final String reason;
    private final boolean fullyKilled;

    public static SPacketDisconnect newInstance(String packet)
    {
        SPacketDisconnect packetDisconnect;
        try
        {
            packetDisconnect = new SPacketDisconnect(packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetDisconnect;
    }

    /**
     * Will exception if the packet string is malformed
     */
    private SPacketDisconnect(String packet)
    {
        JsonObject object = JsonParser.parseString(packet).getAsJsonObject();

        this.reason = object.get("Reason").getAsString();
        this.fullyKilled = object.get("Fully-Killed").getAsBoolean();
    }

    @Override
    public void apply(IRCManager ircManager)
    {
        ircManager.clearOutboundPackets();

        String message = "You were";

        if (this.fullyKilled)
        {
            message += " permanently";
        }

        message += " disconnected from the online users network.";


        String finalMessage = String.format(
                message + " %s",
                this.reason
        );

        Loader.error(finalMessage);

        ircManager.addToChat(Formatting.RED + message);
        ircManager.addToChat(Formatting.RED + "Reason: " + this.reason);

        if (this.fullyKilled)
        {
            ircManager.pinger.shutdown();
            ircManager.manager.shutdown();
            ircManager.FULLY_KILLED = true;
        } else
        {
            ircManager.addToChat("Attempting to reconnect...");
        }

        ircManager.CONNECTED = false;
    }
}
