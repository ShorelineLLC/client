package net.shoreline.client.impl.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.gui.screen.ChatScreenEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class CommandManager extends GenericFeature
{
    private String chatPrefix = ".";

    private final List<Command> commands = new ArrayList<>();

    private final CommandDispatcher<CommandSource> dispatcher;
    private final CommandSource source;

    public CommandManager()
    {
        super("Commands");
        this.dispatcher = new CommandDispatcher<>();
        this.source = new ClientCommandSource(mc.getNetworkHandler(), mc);

        EventBus.INSTANCE.subscribe(this);

        registerCommands(
                new BindCommand(),
                new DrawnCommand(),
                new EnemyCommand(),
                new FolderCommand(),
                new FriendCommand(),
                new HelpCommand(),
                new HistoryCommand(),
                new KitCommand(),
                new MacroCommand(),
                new NotifyCommand(),
                new PrefixCommand(),
                new PresetCommand(),
                new ToggleCommand()
        );

        for (Module module : Managers.MODULES.getModules())
        {
            registerCommand(new ModuleCommand(module));
        }

        for (Command command : commands)
        {
            for (LiteralArgumentBuilder<CommandSource> argumentBuilder : command.getArgumentBuilders())
            {
                command.buildCommand(argumentBuilder);
                dispatcher.register(argumentBuilder);
            }
        }
    }

    @EventListener(priority = 999)
    public void onSendMessage(ChatScreenEvent.SendMessage event)
    {
        String text = event.getChatText().trim();
        if (text.startsWith(chatPrefix))
        {
            event.cancel();
            mc.inGameHud.getChatHud().addToMessageHistory(text);
            try
            {
                String literal = text.substring(1);
                execute(literal);
            } catch (Exception exception)
            {
                // exception.printStackTrace();
            }
        }
    }

    public void execute(String string) throws Exception
    {
        dispatcher.execute(string, source);
    }

    private void registerCommand(Command command)
    {
        commands.add(command);
    }

    private void registerCommands(Command... commands)
    {
        Arrays.stream(commands).forEach(this::registerCommand);
    }
}
