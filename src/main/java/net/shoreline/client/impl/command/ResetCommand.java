package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.ModuleArgumentType;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.util.chat.ChatUtil;

public class ResetCommand extends Command
{

    public ResetCommand()
    {
        super("Reset", "Resets the values of modules", literal("reset"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("module", ModuleArgumentType.module()).executes(context ->
        {
            Module module = ModuleArgumentType.getModule(context, "module");
            if (module == null)
            {
                ChatUtil.error("Invalid module!");
                return 0;
            }
            for (Config<?> config : module.getConfigs())
            {
                if (config.getName().equalsIgnoreCase("Enabled") || config.getName().equalsIgnoreCase("Keybind")
                        || config.getName().equalsIgnoreCase("Hidden"))
                {
                    continue;
                }
                config.resetValue();
            }
            ChatUtil.clientSendMessage("§7" + module.getName() + "§f settings were reset to default values");
            return 1;
        })).executes(context ->
        {
            ChatUtil.error("Must provide module to reset!");
            return 1;
        });
    }
}
