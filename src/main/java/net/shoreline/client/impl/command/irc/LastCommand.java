package net.shoreline.client.impl.command.irc;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.client.CPacketDirectMessage;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.util.chat.ChatUtil;

public final class LastCommand extends Command
{
    public LastCommand()
    {
        super("Last", "Send another message to the last user you messaged.", literal("last", "l"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("message", StringArgumentType.greedyString())
                .executes(c ->
                {
                    OnlineUser user = IRCManager.getInstance().getLastMessagedUser();
                    String message = c.getArgument("message", String.class);

                    if (user == null)
                    {
                        ChatUtil.error("You have not sent any messages.");
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
