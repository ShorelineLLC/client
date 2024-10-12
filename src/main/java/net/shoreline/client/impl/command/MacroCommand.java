package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.util.chat.ChatUtil;

public class MacroCommand extends Command
{
    /**
     *
     */
    public MacroCommand()
    {
        super("Macro", "Creates a new macro preset", literal("macro"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("save/load", StringArgumentType.string()).suggests(suggest("save", "load"))
                .then(argument("macro_name", StringArgumentType.string()).executes(c ->
                {
                    String action = StringArgumentType.getString(c, "save/load");
                    String name = StringArgumentType.getString(c, "macro_name");
                    if (action.equalsIgnoreCase("save"))
                    {
                        Shoreline.CONFIG.saveKeybindConfiguration(name);
                        ChatUtil.clientSendMessage("Saved macro: §s" + name);
                    }
                    else if (action.equalsIgnoreCase("load"))
                    {
                        if (Shoreline.CONFIG.loadKeybindConfiguration(name))
                        {
                            ChatUtil.clientSendMessage("Loaded macro: §s" + name);
                        }
                    }
                    return 1;
                })).executes(c ->
                {
                    ChatUtil.error("Must provide a macro to load!");
                    return 1;
                })).executes(c ->
        {
            ChatUtil.error("Invalid usage! Usage: " + getUsage());
            return 1;
        });
    }
}
