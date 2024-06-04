package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;

/**
 * @author Shoreline
 * @since 1.0
 */
public class DisableAllCommand extends Command
{
    /**
     *
     */
    public DisableAllCommand()
    {
        super("DisableAll", "Disables all enabled modules", literal("disableall"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.executes(c ->
        {
            for (Module module : Managers.MODULE.getModules())
            {
                if (module instanceof ToggleModule toggleModule && toggleModule.isEnabled())
                {
                    toggleModule.disable();
                }
            }
            ChatUtil.clientSendMessage("All modules are disabled");
            return 1;
        });
    }
}
