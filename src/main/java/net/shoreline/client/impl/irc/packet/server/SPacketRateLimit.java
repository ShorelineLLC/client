package net.shoreline.client.impl.irc.packet.server;

import net.minecraft.util.Formatting;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.ServerPacket;

public final class SPacketRateLimit extends ServerPacket
{
    @Override
    public void apply(IRCManager ircManager)
    {
        ircManager.addToChat(Formatting.RED + "You have been rate limited.");
    }
}
