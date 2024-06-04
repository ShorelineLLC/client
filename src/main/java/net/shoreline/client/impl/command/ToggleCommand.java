package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.ModuleArgumentType;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.util.chat.ChatUtil;

/**
 * @author linus
 * @since 1.0
 */
public class ToggleCommand extends Command
{
    public ToggleCommand()
    {
        super("Toggle", "Enables/Disables a module", literal("toggle"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("module", ModuleArgumentType.module()).executes(c ->
        {
            Module module = ModuleArgumentType.getModule(c, "module");
            if (module instanceof ToggleModule t)
            {
                t.toggle();
                ChatUtil.clientSendMessage("%s is now %s", "§7" + t.getName() + "§f", t.isEnabled() ? "§senabled§f" : "§cdisabled§f");
            }
            return 1;
        })).executes(c ->
        {
            ChatUtil.error("Must provide module to toggle!");
            return 1;
        });
    }
}
