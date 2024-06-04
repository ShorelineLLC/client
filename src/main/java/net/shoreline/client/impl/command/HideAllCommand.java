package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;

public class HideAllCommand extends Command
{

    public HideAllCommand()
    {
        super("HideAll", "Hides all modules from the arraylist", literal("hideall"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.executes(c ->
        {
            for (Module module : Managers.MODULE.getModules())
            {
                if (module instanceof ToggleModule toggleModule && !toggleModule.isHidden())
                {
                    toggleModule.setHidden(true);
                }
            }
            ChatUtil.clientSendMessage("All modules are hidden");
            return 1;
        });
    }
}
