package net.shoreline.server.command.commands;

import net.shoreline.server.ServerMain;
import net.shoreline.server.command.Command;
import net.shoreline.server.route.irc.IRCEndpoint;

public final class SendIRCMessageCommand extends Command
{
    @Override
    public void execute(String[] args,
                        boolean askConfirm) throws Throwable
    {
        String message = String.join(" ", args);
        IRCEndpoint.IRC.broadcastServerMessage(message, false);

        ServerMain.LOGGER.info("Broadcasted message: " + message);
    }

    @Override
    public String getUsage()
    {
        return "sendircmessage <message>";
    }

    @Override
    public String getDescription()
    {
        return "Broadcasts a server IRC message.";
    }
}
