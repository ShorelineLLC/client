package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.PlayerArgumentType;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;

/**
 * @author linus
 * @since 1.0
 */
public class FriendCommand extends Command
{
    /**
     *
     */
    public FriendCommand()
    {
        super("Friend", "Adds/Removes a friend from the player list", literal("friend"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("add/del", StringArgumentType.string())
                .suggests(suggest("add", "del", "delete", "remove", "list"))
                .then(argument("friend_name", PlayerArgumentType.player()).executes(c ->
                {
                    String playerName = PlayerArgumentType.getPlayer(c, "friend_name");
                    final String action = StringArgumentType.getString(c, "add/del");
                    if (action.equalsIgnoreCase("add"))
                    {
                        if (Managers.SOCIAL.isFriendInternal(playerName))
                        {
                            ChatUtil.error("Player is already friended!");
                            return 0;
                        }
                        ChatUtil.clientSendMessage("Added friend with name §s" + playerName);
                        Managers.SOCIAL.addFriend(playerName);
                    }
                    else if (action.equalsIgnoreCase("remove") || action.equalsIgnoreCase("del") || action.equalsIgnoreCase("delete"))
                    {
                        if (!Managers.SOCIAL.isFriendInternal(playerName))
                        {
                            ChatUtil.error("Player is not friended!");
                            return 0;
                        }
                        ChatUtil.clientSendMessage("Removed friend with name §c" + playerName);
                        Managers.SOCIAL.remove(playerName);
                    }
                    return 1;
                })).executes(c ->
                {
                    final String action = StringArgumentType.getString(c, "add/del");
                    if (action.equalsIgnoreCase("list"))
                    {
                        if (Managers.SOCIAL.getFriends().isEmpty())
                        {
                            ChatUtil.error("No friends in list!");
                            return 0;
                        }
                        ChatUtil.clientSendMessage("§7Friends: §f" + String.join(", ", Managers.SOCIAL.getFriends()));
                        return 1;
                    }
                    ChatUtil.error("Must provide player to friend!");
                    return 1;
                })).executes(c ->
        {
            ChatUtil.error("Invalid usage! Usage: " + getUsage());
            return 1;
        });
    }
}
