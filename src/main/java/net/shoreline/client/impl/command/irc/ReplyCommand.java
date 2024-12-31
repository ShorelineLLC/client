package net.shoreline.client.impl.command.irc;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.client.CPacketDirectMessage;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.util.chat.ChatUtil;

public final class ReplyCommand extends Command
{
    public ReplyCommand()
    {
        super("Reply", "Reply to the user who last messaged you.", literal("reply", "r"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("message", StringArgumentType.greedyString())
                .executes(c ->
                {
                    OnlineUser user = IRCManager.getInstance().getLastMessageReceivedUser();
                    String message = c.getArgument("message", String.class);

                    if (user == null)
                    {
                        ChatUtil.error("You have not received any messages.");
                        return 0;
                    }

                    CPacketDirectMessage packet = new CPacketDirectMessage(user, message);
                    IRCManager.getInstance().sendPacket(packet);
                    return 1;
                })
        ).executes(context ->
        {
            ChatUtil.error("Invalid usage! Usage: " + getUsage());
            return 1;
        });
    }
}
