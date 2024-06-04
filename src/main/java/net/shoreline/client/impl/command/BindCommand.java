package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.ModuleArgumentType;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.util.KeyboardUtil;
import net.shoreline.client.util.chat.ChatUtil;
import org.lwjgl.glfw.GLFW;

/**
 * @author linus
 * @since 1.0
 */
public class BindCommand extends Command
{
    /**
     *
     */
    public BindCommand()
    {
        super("Bind", "Keybinds a module", literal("bind"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("module", ModuleArgumentType.module())
                .then(argument("key", StringArgumentType.string())
                        .executes(c ->
                        {
                            Module module = ModuleArgumentType.getModule(c, "module");
                            if (module instanceof ToggleModule t)
                            {
                                final String key = StringArgumentType.getString(c, "key");
                                if (key == null)
                                {
                                    ChatUtil.error("Invalid key!");
                                    return 0;
                                }
                                int keycode = KeyboardUtil.getKeyCode(key);
                                if (keycode == GLFW.GLFW_KEY_UNKNOWN)
                                {
                                    ChatUtil.error("Failed to parse key!");
                                    return 0;
                                }
                                t.keybind(keycode);
                                ChatUtil.clientSendMessage("§7%s§f is now bound to §s%s", module.getName(), key.toUpperCase());
                            }
                            return 1;
                        }))
                .executes(c ->
                {
                    ChatUtil.error("Must provide a module to keybind!");
                    return 1;
                })).executes(c ->
        {
            ChatUtil.error("Invalid usage! Usage: " + getUsage());
            return 1;
        });
    }
}
