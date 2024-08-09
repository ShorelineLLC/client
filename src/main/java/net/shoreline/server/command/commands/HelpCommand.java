package net.shoreline.server.command.commands;

import net.shoreline.server.ServerMain;
import net.shoreline.server.command.Command;
import net.shoreline.server.command.CommandManager;

public final class HelpCommand extends Command
{
    @Override
    public void execute(String[] args,
                        boolean askConfirm)
    {
        ServerMain.LOGGER.info("------------ Help ------------");
        ServerMain.LOGGER.info("");

        for (Command command : CommandManager.getRegisteredCommands())
        {
            ServerMain.LOGGER.info("Command: {}", command.getUsage());
            ServerMain.LOGGER.info("Description: {}", command.getDescription());
            ServerMain.LOGGER.info("");
        }

        ServerMain.LOGGER.info("------------------------------");
    }

    @Override
    public String getUsage()
    {
        return "help";
    }

    @Override
    public String getDescription()
    {
        return "Displays all registered commands and their usages.";
    }
}
