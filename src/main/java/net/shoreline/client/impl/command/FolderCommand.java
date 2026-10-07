package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.util.DesktopUtil;

public class FolderCommand extends Command
{
    public FolderCommand()
    {
        super("openfolder", new String[]{"folder"} , "Opens the shoreline folder");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.executes(context ->
        {
            try
            {
                DesktopUtil.open(Shoreline.CONFIG.getSaveDirectory().toFile());
                return 1;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                sendErrorChatMessage("Failed to open the shoreline folder");
            }

            return 0;
        });
    }
}
