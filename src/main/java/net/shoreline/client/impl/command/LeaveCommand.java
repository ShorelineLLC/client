package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.util.chat.ChatUtil;

public class LeaveCommand extends Command
{
    public LeaveCommand()
    {
        super("Leave", "Leaves the game without disconnecting from the server", literal("leave"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("unload", BoolArgumentType.bool()).executes(c ->
        {
            if (mc.isInSingleplayer())
            {
                ChatUtil.error("Not connected to a server!");
                return 0;
            }
            boolean unload = BoolArgumentType.getBool(c, "unload");
            if (unload)
            {
                mc.joinWorld(null);
            }
            mc.setScreen(new MultiplayerScreen(new TitleScreen()));
            return 1;
        })).executes(c ->
        {
            if (mc.isInSingleplayer())
            {
                ChatUtil.error("Not connected to a server!");
                return 0;
            }
            mc.setScreen(new MultiplayerScreen(new TitleScreen()));
            return 1;
        });
    }
}
