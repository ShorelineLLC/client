package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.argtype.PlayerArgumentType;
import net.shoreline.client.util.LookupUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HistoryCommand extends Command
{
    public HistoryCommand()
    {
        super("history", "Shows the name history of a player");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.then(buildArgument("player", PlayerArgumentType.player()).executes(context ->
        {
            String playerName = PlayerArgumentType.getPlayer(context, "player");
            UUID uuid = LookupUtil.getUUID(playerName);
            if (uuid == null)
            {
                sendErrorChatMessage("Failed to find player UUID.");
                return 0;
            }

            Map<String, String> history = LookupUtil.getHistory(uuid);
            if (history == null)
            {
                sendErrorChatMessage("Failed to find player name history.");
                return 0;
            }

            boolean first = true;
            sendClientChatMessage("History:");
            for (Map.Entry<String, String> entry : history.entrySet())
            {
                Formatting format  = first ? Formatting.GOLD : Formatting.WHITE;
                String name        = entry.getValue();
                String nameHistory = entry.getKey().substring(0, 10);
                sendClientChatMessage(format + name + " - " + nameHistory);
                first = false;
            }

            return 1;
        })).executes(context ->
        {
            sendErrorChatMessage("Please provide a player.");
            return 0;
        });
    }
}
