package net.shoreline.client.impl.manager.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.command.*;
import net.shoreline.client.impl.command.irc.*;
import net.shoreline.client.impl.event.gui.chat.ChatMessageEvent;
import net.shoreline.client.impl.event.gui.screen.SuggestChatEvent;
import net.shoreline.client.impl.event.keyboard.KeyboardInputEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.loader.Loader;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author linus
 * @see Command
 * @since 1.0
 */
public class CommandManager implements Globals
{
    //
    private final List<Command> commands = new ArrayList<>();
    // Command prefix, used to identify a command in the chat
    private String prefix = ".";
    private int prefixKey = GLFW.GLFW_KEY_PERIOD;

    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
    private final CommandSource source = new ClientCommandSource(null, mc);

    /**
     * Registers commands to the CommandManager
     */
    public CommandManager()
    {
        EventBus.INSTANCE.subscribe(this);

        String rank = Loader.SESSION.getUserType();

        if (rank.equals("dev"))
        {
            register(new BroadcastServerMsgCommand());
            register(new DirectServerMsgCommand());
            register(new MuteCommand());
            register(new UnmuteCommand());
        }

        if (rank.equals("dev") || rank.equals("beta"))
        {
            register(new CloakCommand());
        }

        register(
                new BindCommand(),
                new ConfigCommand(),
                new ConnectCommand(),
                new CoordsCommand(),
                new DirectMessageCommand(),
                new DisableAllCommand(),
                new DrawnCommand(),
                new CustomFontCommand(),
                new FriendCommand(),
                new HClipCommand(),
                new HelpCommand(),
                new HideAllCommand(),
                new HistoryCommand(),
                new LastCommand(),
                new LeaveCommand(),
                new LoadCommand(),
                new MacroCommand(),
                new ModulesCommand(),
                new NbtCommand(),
                new NotifyCommand(),
                new OnlineCommand(),
                new OpenFolderCommand(),
                new PrefixCommand(),
                new QueueCommand(),
                // new RegearCommand(),
                new ResetCommand(),
                new ResetGuiCommand(),
                new SaveCommand(),
                new ReloadSoundCommand(),
                new ReplyCommand(),
                new SkinGrabCommand(),
                new StatsCommand(),
                new ToggleCommand(),
                new VanishCommand(),
                new VClipCommand(),
                new WaypointCommand(),
                new YawCommand()
        );

        for (Module module : Managers.MODULE.getModules())
        {
            register(new ModuleCommand(module));
        }
        Shoreline.info("Registered {} commands!", commands.size());
        for (Command command : commands)
        {
            for (LiteralArgumentBuilder<CommandSource> builder : command.getCommandBuilders())
            {
                command.buildCommand(builder);
                dispatcher.register(builder);
            }
        }
    }

    @EventListener(priority = 999)
    public void onChatMessage(ChatMessageEvent.Client event)
    {
        final String text = event.getMessage().trim();
        if (text.startsWith(prefix))
        {
            String literal = text.substring(1);
            event.cancel();
            mc.inGameHud.getChatHud().addToMessageHistory(text);
            try
            {
                dispatcher.execute(dispatcher.parse(literal, source));
            }
            catch (Exception exception)
            {
                // exception.printStackTrace();
            }
        }
    }

    @EventListener
    public void onKeyboardInput(KeyboardInputEvent event)
    {
        if (event.getAction() == 1 && event.getKeycode() == prefixKey && mc.currentScreen == null)
        {
            event.cancel();
            mc.setScreen(new ChatScreen(""));
        }
    }

    @EventListener
    public void onChatSuggest(SuggestChatEvent event)
    {
        event.setPrefix(prefix);
        event.setDispatcher(dispatcher);
        event.setSource(source);
    }

    @SuppressWarnings("unchecked")
    private LiteralArgumentBuilder<Object> redirectBuilder(String alias, LiteralCommandNode<?> destination)
    {
        LiteralArgumentBuilder<Object> literalArgumentBuilder = LiteralArgumentBuilder.literal(alias.toLowerCase()).requires((Predicate<Object>) destination.getRequirement())
                .forward((CommandNode<Object>) destination.getRedirect(), (RedirectModifier<Object>) destination.getRedirectModifier(), destination.isFork())
                .executes((com.mojang.brigadier.Command<Object>) destination.getCommand());
        for (CommandNode<?> child : destination.getChildren())
        {
            literalArgumentBuilder.then((CommandNode<Object>) child);
        }
        return literalArgumentBuilder;
    }

    /**
     * @param commands
     */
    private void register(Command... commands)
    {
        for (Command command : commands)
        {
            register(command);
        }
    }

    /**
     * @param command
     */
    private void register(Command command)
    {
        commands.add(command);
    }

    /**
     * @return
     */
    public List<Command> getCommands()
    {
        return commands;
    }

    public Command getCommand(String name)
    {
        for (Command command : commands)
        {
            if (command.getName().equalsIgnoreCase(name))
            {
                return command;
            }
        }
        return null;
    }

    public String getPrefix()
    {
        return prefix;
    }

    /**
     * @param prefix
     * @param prefixKey
     */
    public void setPrefix(String prefix, int prefixKey)
    {
        this.prefix = prefix;
        this.prefixKey = prefixKey;
    }

    public CommandDispatcher<CommandSource> getDispatcher()
    {
        return dispatcher;
    }

    public CommandSource getSource()
    {
        return source;
    }
}
