package net.shoreline.client.impl.command;

import com.mojang.brigadier.CommandDispatcher;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.api.command.Command;
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
                new DrawnCommand(),
                new FriendCommand(),
                new KitCommand(),
                new NotifyCommand(),
                new PrefixCommand(),
                new PresetCommand()
        );

        for (Command command : commands)
        {
            command.buildCommand();
            dispatcher.register(command.getArgumentBuilder());
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
                dispatcher.execute(dispatcher.parse(literal, source));
            } catch (Exception exception)
            {
                // exception.printStackTrace();
            }
        }
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
