package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.util.chat.ChatUtil;

import java.util.ArrayList;
import java.util.List;

public final class OnlineCommand extends Command
{
    public OnlineCommand()
    {
        super("Online", "Lists all online Shoreline users", literal("online"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.executes(c ->
        {
            if (!IRCManager.getInstance().CONNECTED)
            {
                ChatUtil.error("You are not connected to the online users network.");
                return 1;
            }

            List<String> playersList = new ArrayList<>();

            for (OnlineUser onlineUser : IRCManager.getInstance().getAllOnlineUsers())
            {
                playersList.add(onlineUser.getUsertype().getColorCode() + onlineUser.getName());
            }

            IRCManager.getInstance().addToChat("Online Users: " + String.join(", ", playersList));

            return 1;
        });
    }
}
