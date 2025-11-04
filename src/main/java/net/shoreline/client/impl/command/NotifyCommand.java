package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.argtype.ModuleArgumentType;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.Toggleable;

public class NotifyCommand extends Command
{
    public NotifyCommand()
    {
        super("notify", "Notifies in chat on module toggle");
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
                                boolean notify = !toggle.shouldNotify();
                                toggle.setNotify(notify);
                                sendClientMessageWithOptionalDeletion((notify ? "Added" : "Removed") + " notifications for " + Formatting.GRAY + module.getName(), toggle.hashCode());
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
