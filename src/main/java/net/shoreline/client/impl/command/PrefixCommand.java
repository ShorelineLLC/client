package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.impl.Managers;

public class PrefixCommand extends Command
{
    public PrefixCommand()
    {
        super("prefix", "Changes the command prefix");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.then(buildArgument("prefix", StringArgumentType.string())
                .executes(context ->
                {
                    final String prefix = StringArgumentType.getString(context, "prefix");
                    if (prefix.length() > 1)
                    {
                        sendErrorChatMessage("Prefix can only be one character!");
                        return 0;
                    }

                    Managers.COMMANDS.setChatPrefix(prefix);
                    sendClientMessageWithOptionalDeletion("Command prefix changed to " + prefix, hashCode());
                    return 1;
                }))

                .executes(context ->
                {
                    sendErrorChatMessage("Please provide a new prefix!");
                    return 1;
                }
        );
    }
}
