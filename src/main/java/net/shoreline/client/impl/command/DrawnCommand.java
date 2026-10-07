package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.argtype.ModuleArgumentType;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.Toggleable;

public class DrawnCommand extends Command
{
    public DrawnCommand()
    {
        super("drawn", new String[] {"d"}, "Toggles drawn state in arraylist");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.then(buildArgument("module", ModuleArgumentType.module())
                .executes(c ->
                {
                    Module module = ModuleArgumentType.getModule(c, "module");
                    if (module instanceof Toggleable toggle)
                    {
                        boolean hide = !toggle.isHidden();
                        toggle.setHidden(hide);
                        sendClientChatMessage(Formatting.GRAY + module.getName() + Formatting.RESET + " is now " +
                                (hide ? Formatting.RED + "hidden" : Formatting.GREEN + "visible"));
                    }

                    return 1;
                }))

                .executes(c ->
                {
                    sendErrorChatMessage("Must provide module!");
                    return 1;
                });
    }
}
