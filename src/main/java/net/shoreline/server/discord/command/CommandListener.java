package net.shoreline.server.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.shoreline.server.discord.command.commands.PurchaseCommand;
import net.shoreline.server.discord.command.commands.ResetHWIDCommand;
import net.shoreline.server.discord.command.commands.TOSCommand;

import java.util.Set;

public final class CommandListener extends ListenerAdapter
{
    private static final CommandListener INSTANCE = new CommandListener();

    private final Set<Command> commands = Set.of(
            new PurchaseCommand(),
            new ResetHWIDCommand(),
            new TOSCommand()
    );

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        Command command = this.commands.stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(event.getName()))
                .findFirst()
                .orElse(null);

        if (command != null)
        {
            EmbedBuilder builder = new EmbedBuilder();
            command.execute(event, builder);
            event.replyEmbeds(builder.build()).queue();
        }
    }

    public static CommandListener getInstance()
    {
        return INSTANCE;
    }
}
