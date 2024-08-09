package net.shoreline.server.command;

import net.shoreline.server.ServerMain;

import java.util.Scanner;

public abstract class Command
{
    public abstract void execute(String[] args,
                                 boolean askConfirm) throws Throwable;

    public abstract String getUsage();

    public abstract String getDescription();

    protected final boolean askForConfirmation(String info)
    {
        ServerMain.LOGGER.warn("{} Are you sure you want to execute this command? (y/n)", info);

        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine().toLowerCase();

        if (answer.equals("y"))
        {
            return true;
        }

        ServerMain.LOGGER.info("Cancelled the command.");
        return false;
    }
}
