package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;

public class RenameCommand extends Command
{
    public RenameCommand()
    {
        super("rename", "Allows you to change the name of the client");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {

    }
}
