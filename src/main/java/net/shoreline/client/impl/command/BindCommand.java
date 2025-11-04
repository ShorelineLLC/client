package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.argtype.ModuleArgumentType;
import net.shoreline.client.api.macro.ModuleKeybind;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.render.ClientFormatting;
import net.shoreline.client.util.Keyboard;
import org.lwjgl.glfw.GLFW;

public class BindCommand extends Command
{
    public BindCommand()
    {
        super("bind", new String[] {"keybind"}, "Binds a module to a key");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.then(buildArgument("module", ModuleArgumentType.module())
                .then(buildArgument("key", StringArgumentType.string())
                        .executes(c ->
                        {
                            Module module = ModuleArgumentType.getModule(c, "module");
                            if (module instanceof Toggleable t)
                            {
                                final String key = StringArgumentType.getString(c, "key");
                                if (key == null)
                                {
                                    sendErrorChatMessage("Invalid key!");
                                    return 0;
                                }

                                int keycode = Keyboard.getKeyCode(key);
                                if (keycode == GLFW.GLFW_KEY_UNKNOWN)
                                {
                                    sendErrorChatMessage("Failed to parse key!");
                                    return 0;
                                }

                                t.setKeybind(new ModuleKeybind(keycode, t));
                                sendClientChatMessage(Formatting.GRAY + module.getName() + Formatting.RESET + " is now bound to " + ClientFormatting.THEME + key.toUpperCase());
                            }

                            return 1;
                        }))

                .executes(c ->
                {
                    sendErrorChatMessage("Must provide a module to keybind!");
                    return 1;
                }))

                .executes(c ->
                {
                    sendErrorChatMessage("Invalid usage! Usage: bind <module> <key_name>");
                    return 1;
                });
    }
}
