package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.util.chat.ChatUtil;

public class SaveCommand extends Command
{
    public SaveCommand()
    {
        super("Save", "Saves the current config", literal("save"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.executes(c ->
        {
            Shoreline.CONFIG.saveClientModules();
            ChatUtil.clientSendMessage("Saved client config!");
            return 1;
        });
    }
}
