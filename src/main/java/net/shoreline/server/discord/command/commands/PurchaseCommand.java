package net.shoreline.server.discord.command.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.shoreline.server.discord.command.Command;

import java.awt.*;

public final class PurchaseCommand extends Command
{
    public PurchaseCommand()
    {
        super("purchase");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event,
                        EmbedBuilder builder)
    {
        builder.setTitle("Purchase");

        builder.addField(
                "Release",
                "Shoreline release will available for purchase shortly at https://shorelineclient.net/purchase/",
                false
        );

        builder.addField(
                "Beta",
                "Shoreline is currently available for Beta purchase. To buy, DM an LLC member.",
                false
        );

        builder.setColor(new Color(39, 127, 196));
    }
}
