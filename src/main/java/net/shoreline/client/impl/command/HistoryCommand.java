package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.PlayerArgumentType;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class HistoryCommand extends Command {

    public HistoryCommand()
    {
        super("History", "View the name history of a player", literal("history"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("player", PlayerArgumentType.player()).executes(c ->
        {
            String playerName = PlayerArgumentType.getPlayer(c, "player");
            UUID uuid = Managers.LOOKUP.getUUIDFromName(playerName);
            if (uuid == null)
            {
                ChatUtil.error("Could not find player UUID!");
                return 0;
            }
            Map<Date, String> nameHistory = Managers.LOOKUP.getNameHistoryFromUUID(uuid);
            if (nameHistory == null)
            {
                ChatUtil.error("Could not find player name history!");
                return 0;
            }
            ArrayList<String> nameHistoryList = new ArrayList<>();
            for (Map.Entry<Date, String> entry : nameHistory.entrySet())
            {
                String date = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss").format(entry.getKey());
                nameHistoryList.add(entry.getValue() + " - " + date);
            }
            if (nameHistoryList.isEmpty())
            {
                ChatUtil.error("No player name history!");
                return 0;
            }
            ChatUtil.clientSendMessageRaw("§7History: §f" + String.join(", ", nameHistoryList));
            return 1;
        }));
    }
}
