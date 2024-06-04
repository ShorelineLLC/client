package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.Hideable;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.ModuleArgumentType;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.util.chat.ChatUtil;

/**
 * @author linus
 * @see Hideable
 * @since 1.0
 */
public class DrawnCommand extends Command
{
    public DrawnCommand()
    {
        super("Drawn", "Toggles the drawn state of the module", literal("drawn"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("module", ModuleArgumentType.module())
                .executes(c ->
                {
                    Module module = ModuleArgumentType.getModule(c, "module");
                    if (module instanceof ToggleModule toggle)
                    {
                        boolean hide = !toggle.isHidden();
                        toggle.setHidden(hide);
                        ChatUtil.clientSendMessage("§7" + module.getName() + "§f is now " +
                                (hide ? "§chidden§f" : "§svisible§f") + Formatting.RESET + " in the Hud");
                    }
                    return 1;
                })).executes(c ->
        {
            ChatUtil.error("Must provide module to draw!");
            return 1;
        });
    }
}
