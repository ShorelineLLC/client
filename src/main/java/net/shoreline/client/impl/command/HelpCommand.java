package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.render.ClientFormatting;
import net.shoreline.client.util.text.Formatter;

public class HelpCommand extends Command
{
    public HelpCommand()
    {
        super("help", "Shows a list of commands and their descriptions");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.executes(context ->
        {
            for (Command command : Managers.COMMANDS.getCommands())
            {
                MutableText text = Text.literal(ClientFormatting.THEME + Formatter.capitalize(command.getName()));
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getSuggestion(command));
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(command.getDescription()));
                Style style = Style.EMPTY.withClickEvent(clickEvent).withHoverEvent(hoverEvent);

                sendClientTextMessage(text.setStyle(style));
            }

            return 1;
        });
    }

    public String getSuggestion(Command command)
    {
        return Managers.COMMANDS.getChatPrefix() + command.getName();
    }
}
