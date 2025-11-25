package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.argtype.PlayerArgumentType;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.render.ClientFormatting;

import java.util.Set;

public class FriendCommand extends Command
{
    public FriendCommand()
    {
        super("friend", "Adds/Removes from the friend list");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.then(buildArgument("add/remove", StringArgumentType.string())
                        .suggests(buildSuggestions("add", "del", "delete", "remove", "list"))
                        .then(buildArgument("friend name", PlayerArgumentType.player())
                                .executes(context ->
                                {
                                    String action = StringArgumentType.getString(context, "add/remove");
                                    String friendName = StringArgumentType.getString(context, "friend name");
                                    if (action.equalsIgnoreCase("add"))
                                    {
                                        if (Managers.SOCIAL.isFriendInternal(friendName))
                                        {
                                            sendErrorChatMessage("Player is already friended!");
                                            return 0;
                                        }

                                        sendClientChatMessage("Added friend with name " + ClientFormatting.THEME + friendName);
                                        Managers.SOCIAL.addFriend(friendName);

                                    } else if (action.equalsIgnoreCase("del") || action.equalsIgnoreCase("delete") || action.equalsIgnoreCase("remove"))
                                    {
                                        if (!Managers.SOCIAL.isFriendInternal(friendName))
                                        {
                                            sendErrorChatMessage("Player is not friended!");
                                            return 0;
                                        }

                                        sendClientChatMessage("Removed friend with name " + Formatting.RED + friendName);
                                        Managers.SOCIAL.removeFriend(friendName);
                                    }

                                    return 1;
                                }))

                        .executes(context ->
                        {
                            String action = StringArgumentType.getString(context, "add/remove");
                            if (action.equalsIgnoreCase("list"))
                            {
                                Set<String> friendNames = Managers.SOCIAL.getFriends();
                                if (friendNames.isEmpty())
                                {
                                    sendErrorChatMessage("You have no players friended!");
                                    return 0;
                                }

                                sendClientChatMessage(Formatting.GRAY + "Friends: " + Formatting.WHITE + String.join(", ", friendNames));
                                return 1;
                            }

                            return 1;
                        }))

                .executes(context ->
                {
                    sendErrorChatMessage("Invalid command usage! Usage: friend <save/delete/list> *<friend_name>");
                    return 1;
                });
    }
}
