package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.PlayerArgumentType;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;

import java.util.Map;

public class StatsCommand extends Command
{
    public StatsCommand()
    {
        super("Stats", "Shows 2b2t player stats", literal("stats"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("player", PlayerArgumentType.player()).executes(c ->
        {
            String playerName = PlayerArgumentType.getPlayer(c, "player");
            Map<String, String> stats = Managers.LOOKUP.getPlayerStats2b2t(playerName);
            if (stats == null)
            {
                ChatUtil.error("Could not find player 2b2t stats!");
                return 0;
            }
            int id = -9957204;
            ChatUtil.clientSendMessage(playerName + "'s Stats", id--);
            for (Map.Entry<String, String> entry : stats.entrySet())
            {
                ChatUtil.clientSendMessageRaw("§7" + entry.getKey() + ": §f" + entry.getValue(), id--);
            }
            return 1;
        }));
    }
}
