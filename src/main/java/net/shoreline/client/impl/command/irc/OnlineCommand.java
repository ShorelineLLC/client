package net.shoreline.client.impl.command.irc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.util.chat.ChatUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
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
            List<OnlineUser> onlineUsers = IRCManager.getInstance().getAllOnlineUsers().stream().sorted(Comparator.comparingInt(u -> -u.getUsertype().getRank())).toList();
            for (OnlineUser onlineUser : onlineUsers)
            {
                playersList.add(onlineUser.getUsertype().getColorCode() + onlineUser.getName());
            }
            LinkedHashSet<String> set = new LinkedHashSet<>(playersList);
            IRCManager.getInstance().addToChat("Online Users: " + String.join(Formatting.GRAY + ", ", set));

            return 1;
        });
    }
}
