package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;

import java.util.ArrayList;
import java.util.List;

public class ModulesCommand extends Command
{
    public ModulesCommand()
    {
        super("Modules", "Displays all client modules", literal("modules"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.executes(c ->
        {
            List<String> modulesList = new ArrayList<>();
            for (Module module : Managers.MODULE.getModules())
            {
                String formatting = module instanceof ToggleModule t && t.isEnabled() ? "§s" : "§f";
                modulesList.add(formatting + module.getName());
            }
            ChatUtil.clientSendMessageRaw(" §7Modules:§f " + String.join(", ", modulesList));
            return 1;
        });
    }
}
