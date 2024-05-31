package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.mixin.accessor.AccessorMinecraftClient;
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
        builder.executes(c ->
        {
            if (mc.isInSingleplayer())
            {
                ChatUtil.error("Not connected to a server!");
                return 0;
            }
            mc.getNetworkHandler().unloadWorld();
            mc.world = null;
            mc.player = null;
            ((AccessorMinecraftClient) mc).hookSetWorld(null);
            mc.setScreenAndRender(new TitleScreen());
            return 1;
        });
    }
}
