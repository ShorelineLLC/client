package net.shoreline.client.impl.command;

import net.minecraft.util.Formatting;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.argtype.ModuleArgumentType;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.Toggleable;

public class DrawnCommand extends Command
{
    public DrawnCommand()
    {
        super("drawn", "Toggles drawn state in arraylist");
    }

    @Override
    public void buildCommand()
    {
        argumentBuilder.then(buildArgument("module", ModuleArgumentType.module())
                .executes(c ->
                {
                    Module module = ModuleArgumentType.getModule(c, "module");
                    if (module instanceof Toggleable toggle)
                    {
                        boolean hide = !toggle.isHidden();
                        toggle.setHidden(hide);
                        sendClientChatMessage(module.getName() + " is now " + (hide ? Formatting.RED + "hidden" : Formatting.GREEN + "visible"));
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
