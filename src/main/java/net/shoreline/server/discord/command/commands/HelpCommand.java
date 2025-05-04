package net.shoreline.server.discord.command.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.shoreline.server.discord.command.Command;

public final class HelpCommand extends Command
{
    public HelpCommand()
    {
        super("help");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event,
                        EmbedBuilder builder)
    {

    }
}
