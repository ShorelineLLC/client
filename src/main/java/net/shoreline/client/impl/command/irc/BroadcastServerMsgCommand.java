package net.shoreline.client.impl.command.irc;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.client.CPacketBroadcastServerMsg;
import net.shoreline.client.util.chat.ChatUtil;

public final class BroadcastServerMsgCommand extends Command
{
    public BroadcastServerMsgCommand()
    {
        super("BroadcastServerMsg", "Broadcasts a server message in the IRC", literal("broadcastservermsg"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(
                argument("message", StringArgumentType.greedyString())
                        .executes(c ->
                        {
                            String argument = c.getArgument("message", String.class);

                            CPacketBroadcastServerMsg packet = new CPacketBroadcastServerMsg(argument);
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
