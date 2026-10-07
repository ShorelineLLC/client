package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.*;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.macro.ModuleKeybind;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.render.ClientFormatting;
import net.shoreline.client.util.Keyboard;
import net.shoreline.client.util.text.Formatter;

public class MacroCommand extends Command
{
    public MacroCommand()
    {
        super("macro", "Builds a macro");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.then(buildArgument("add/remove", StringArgumentType.string())
            .suggests(buildSuggestions("add", "del", "delete", "remove", "list"))
                .executes(context ->
                {
                    String action = StringArgumentType.getString(context, "add/remove");
                    if (action.equalsIgnoreCase("list"))
                    {
                        for (Macro macro : Managers.MACROS.getMacros())
                        {
                            if (macro instanceof ModuleKeybind)
                            {
                                continue;
                            }

                            String hover = macro.getCommand() + " : " + Keyboard.getKeyName(macro.getKeycode());
                            MutableText text = Text.literal(Formatter.capitalize(macro.getName()));
                            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(hover));
                            Style style = Style.EMPTY.withHoverEvent(hoverEvent);
                            sendClientTextMessage(text.setStyle(style));
                        }

                        return 1;
                    }

                    return 0;
                })
            .then(buildArgument("name", StringArgumentType.string())
                .suggests((context, builder) ->
                {
                    String action = StringArgumentType.getString(context, "add/remove");
                    if (action.equalsIgnoreCase("del"))
                    {
                        for (Macro macro : Managers.MACROS.getMacros())
                        {
                            if (macro instanceof ModuleKeybind)
                            {
                                continue;
                            }

                            builder.suggest(macro.getName().toLowerCase());
                        }
                    }

                    return builder.buildFuture();
                })
                .executes(context ->
                {
                    String action = StringArgumentType.getString(context, "add/remove");
                    if (action.equalsIgnoreCase("del"))
                    {
                        String name = StringArgumentType.getString(context, "name");
                        Macro macro = Managers.MACROS.getMacro(name);
                        Managers.MACROS.unregister(macro);
                        sendClientChatMessage("Deleted Macro with name: " + ClientFormatting.THEME + name);
                        return 1;
                    }

                    return 0;
                })
            .then(buildArgument("key", StringArgumentType.string())
            .then(buildArgument("command", StringArgumentType.greedyString())
                .executes(context ->
                {
                    String action  = StringArgumentType.getString(context, "add/remove");
                    String name    = StringArgumentType.getString(context, "name");
                    String command = StringArgumentType.getString(context, "command");
                    String key     = StringArgumentType.getString(context, "key");

                    if (action.equalsIgnoreCase("add"))
                    {
                        int keyCode = Keyboard.getKeyCode(key);
                        Macro macro = new Macro(name, keyCode, command);
                        Managers.MACROS.register(macro);
                        sendClientChatMessage("Added Macro with name: " + ClientFormatting.THEME + name);
                        return 1;
                    }

                    return 0;
                })))));

    }
}
