package net.shoreline.server.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class Command
{
    private final String name;

    public Command(String name)
    {
        this.name = name;
    }

    public abstract void execute(SlashCommandInteractionEvent event,
                                 EmbedBuilder builder);

    public final String getName()
    {
        return this.name;
    }
}
