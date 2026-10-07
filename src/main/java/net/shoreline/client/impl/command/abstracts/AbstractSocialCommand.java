package net.shoreline.client.impl.command.abstracts;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.argtype.PlayerArgumentType;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.render.ClientFormatting;
import net.shoreline.client.impl.social.SocialManager;

import java.util.Set;

public class AbstractSocialCommand extends Command
{
    private final SocialManager.SocialType type;

    public AbstractSocialCommand(SocialManager.SocialType socialType)
    {
        super(socialType.name().toLowerCase(), String.format("Adds/Removes from the %s list", socialType.name().toLowerCase()));
        this.type = socialType;
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.then(buildArgument("add/remove", StringArgumentType.string())
            .suggests(buildSuggestions("add", "del", "delete", "remove", "list"))
            .then(buildArgument("player name", PlayerArgumentType.player())
                .executes(context ->
                {
                    String action = StringArgumentType.getString(context, "add/remove");
                    String playerName = StringArgumentType.getString(context, "player name");
                    if (action.equalsIgnoreCase("add"))
                    {
                        if (Managers.SOCIAL.isFriendInternal(playerName))
                        {
                            sendErrorChatMessage(format("Player is already added as a <type>!"));
                            return 0;
                        }

                        sendClientChatMessage(format("Added <type> with name " + ClientFormatting.THEME + playerName));
                        Managers.SOCIAL.addSocial(playerName, type);
                    }
                    else if (action.equalsIgnoreCase("del") || action.equalsIgnoreCase("delete") || action.equalsIgnoreCase("remove"))
                    {
                        if (Managers.SOCIAL.getType(playerName) != type)
                        {
                            sendErrorChatMessage(format("Player is not added as a <type>!"));
                            return 0;
                        }

                        sendClientChatMessage(format("Removed <type> with name " + Formatting.RED + playerName));
                        Managers.SOCIAL.removeSocial(playerName);
                    }

                    return 1;
                }))
                .executes(context ->
                {
                    String action = StringArgumentType.getString(context, "add/remove");
                    if (action.equalsIgnoreCase("list"))
                    {
                        Set<String> playerNames = Managers.SOCIAL.getTypes(type);
                        if (playerNames.isEmpty())
                        {
                            sendErrorChatMessage(format("You have no <type>s!"));
                            return 0;
                        }

                        sendClientChatMessage(format(Formatting.GRAY + "<type>s: " + Formatting.WHITE + String.join(", ", playerNames)));
                        return 1;
                    }

                    return 1;
                }))
                .executes(context ->
                {
                    sendErrorChatMessage(format("Invalid command usage! Usage: <type> <save/delete/list> *<player_name>"));
                    return 1;
                });
    }

    private String format(String string)
    {
        return string
                .replace("<type>", type.name().toLowerCase())
                .replace("enemys", "enemies"); // this just bothered me
    }
}
