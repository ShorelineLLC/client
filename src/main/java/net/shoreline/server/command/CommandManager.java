package net.shoreline.server.command;

import net.shoreline.server.ServerMain;
import net.shoreline.server.command.commands.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class CommandManager
{
    // for help command
    private static final List<Command> registeredCommands = new ArrayList<>();

    static {
        registeredCommands.add(new HelpCommand());

        registeredCommands.add(new DelUserCommand());
        registeredCommands.add(new FindUserCommand());
        registeredCommands.add(new ResetClassCacheCommand());
        registeredCommands.add(new ResetHWIDCommand());
        registeredCommands.add(new SafeStopCommand());
        registeredCommands.add(new SetHWIDCountCommand());
    }

    public static void startListening()
    {
        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            String command = scanner.nextLine().toLowerCase();

            if (command.equals("stop"))
            {
                System.exit(0);
                return;
            }

            parseCommand(command);
        }
    }

    private static void parseCommand(String input)
    {
        String[] tokens = input.split(" ");

        if (tokens.length == 0)
        {
            ServerMain.LOGGER.error("Invalid command");
        }

        Command command = switch (tokens[0])
        {
            case "deluser" -> new DelUserCommand();
            case "finduser" -> new FindUserCommand();
            case "help" -> new HelpCommand();
            case "resetclasscache" -> new ResetClassCacheCommand();
            case "resethwid" -> new ResetHWIDCommand();
            case "safestop" -> new SafeStopCommand();
            case "sethwidcount" -> new SetHWIDCountCommand();
            default -> null;
        };

        if (command == null)
        {
            ServerMain.LOGGER.error("Unknown command: {}", tokens[0]);
            return;
        }

        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, args.length);

        try
        {
            command.execute(args, true);
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error(t.getMessage());
        }
    }

    public static List<Command> getRegisteredCommands()
    {
        return registeredCommands;
    }
}
