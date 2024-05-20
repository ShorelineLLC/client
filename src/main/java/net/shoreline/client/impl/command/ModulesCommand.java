package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.shoreline.client.Shoreline;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;

public class ModulesCommand extends Command {
    public ModulesCommand() {
        super("Modules", "Displays all client modules", literal("modules"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(c -> {
            StringBuilder modulesList = new StringBuilder();
            for (Module module : Managers.MODULE.getModules()) {
                String formatting = module instanceof ToggleModule t && t.isEnabled() ? "§s" : "§f";
                modulesList.append(formatting);
                modulesList.append(module.getName());
                modulesList.append(Formatting.RESET);
                // LOL
                if (!module.getName().equalsIgnoreCase("Zoom")) {
                    modulesList.append(", ");
                }
            }
            ChatUtil.clientSendMessageRaw(" §7Modules:§f " + modulesList);
            return 1;
        });
    }
}
