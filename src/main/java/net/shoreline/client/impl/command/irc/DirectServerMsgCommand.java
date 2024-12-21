package net.shoreline.client.impl.command.irc;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.OnlineUserArgumentType;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.client.CPacketDirectServerMsg;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.util.chat.ChatUtil;

public final class DirectServerMsgCommand extends Command
{
    public DirectServerMsgCommand()
    {
        super("DirectServerMsg", "Directs a server message to a player in the IRC", literal("directservermsg"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(
                argument("username", OnlineUserArgumentType.user())
                        .then(argument("message", StringArgumentType.greedyString())
                        .executes(c ->
                        {
                            OnlineUser user = c.getArgument("username", OnlineUser.class);
                            String message = c.getArgument("message", String.class);

                            CPacketDirectServerMsg packet = new CPacketDirectServerMsg(user, message);
                            IRCManager.getInstance().sendPacket(packet);

                            return 1;
                        }))
                        .executes(c -> {
                            ChatUtil.error("Invalid usage! Usage: " + getUsage());
                            return 1;
                        })
        ).executes(c ->
        {
            ChatUtil.error("Invalid usage! Usage: " + getUsage());
            return 1;
        });
    }
}
