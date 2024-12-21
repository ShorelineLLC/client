package net.shoreline.client.impl.command.irc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.OnlineUserArgumentType;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.client.CPacketMute;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.util.chat.ChatUtil;

public final class UnmuteCommand extends Command
{
    public UnmuteCommand()
    {
        super("Unmute", "Unmutes an IRC user.", literal("unmute"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(
                argument("username", OnlineUserArgumentType.user())
                        .executes(c ->
                        {
                            OnlineUser user = c.getArgument("username", OnlineUser.class);

                            CPacketMute packet = new CPacketMute(user, false);
                            IRCManager.getInstance().sendPacket(packet);

                            return 1;
                        })
        ).executes(c ->
        {
            ChatUtil.error("Invalid usage! Usage: " + getUsage());
            return 1;
        });
    }
}
