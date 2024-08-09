package net.shoreline.server.command.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.shoreline.server.ServerMain;
import net.shoreline.server.command.Command;
import net.shoreline.server.route.irc.IRCEndpoint;
import net.shoreline.server.route.loader.LoaderEndpoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class SafeStopCommand extends Command
{
    @Override
    public void execute(String[] args,
                        boolean askConfirm) throws Throwable
    {
        // always ask for confirm
        boolean ok = askForConfirmation("This will stop the server.");

        if (ok)
        {
            save();
            System.exit(0);
        }
    }

    @Override
    public String getUsage()
    {
        return "safestop";
    }

    @Override
    public String getDescription()
    {
        return "Stops the server safely, cleaning up and saving resources.";
    }

    public void save()
    {
        ServerMain.LOGGER.info("Saving IRC reconnection tokens...");

        File backups = new File("backups");

        if (!backups.exists())
        {
            backups.mkdir();
        }

        File ircReconnectionTokenBackups = new File(backups, "irc_reconnection_tokens.txt");

        try
        {
            if (ircReconnectionTokenBackups.exists())
            {
                ircReconnectionTokenBackups.delete();
            }

            ircReconnectionTokenBackups.createNewFile();

            try (FileOutputStream fos = new FileOutputStream(ircReconnectionTokenBackups))
            {
                JsonObject json = new JsonObject();
                JsonArray sessions = new JsonArray();

                for (String ircToken : IRCEndpoint.IRC.authorizedBackupTokens)
                {
                    JsonObject session = new JsonObject();

                    session.addProperty("token", ircToken);

                    LoaderEndpoint.IRCSession ircSession = LoaderEndpoint.awaitingTokens.remove(ircToken);

                    if (ircSession == null) // weird, shouldn't happen
                    {
                        continue;
                    }

                    session.addProperty("uid", ircSession.getUID());
                    session.addProperty("username", ircSession.getUsername());
                    session.addProperty("usertype", ircSession.getUsertype());

                    sessions.add(session);
                }

                json.add("sessions", sessions);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                fos.write(gson.toJson(json).getBytes());
            }

            ServerMain.LOGGER.info("Successfully saved the IRC reconnection tokens");
        } catch (IOException e)
        {
            ServerMain.LOGGER.error("Couldn't save the IRC reconnection tokens: ", e);
        }
    }
}
