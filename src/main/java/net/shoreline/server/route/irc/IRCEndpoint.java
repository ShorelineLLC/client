package net.shoreline.server.route.irc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import net.shoreline.server.ServerMain;
import net.shoreline.server.route.irc.server.IRCServer;
import net.shoreline.server.route.loader.LoaderEndpoint;

import java.io.File;
import java.io.FileInputStream;

public final class IRCEndpoint implements EndpointGroup
{
    public static final IRCServer IRC = new IRCServer();

    @Override
    public void addEndpoints()
    {
        ApiBuilder.ws("/", wsConfig ->
        {
            wsConfig.onConnect(IRC.onConnect());
            wsConfig.onMessage(IRC.onMessage());
            wsConfig.onBinaryMessage(IRC.onBinaryMessage());
            wsConfig.onClose(IRC.onClose());
            wsConfig.onError(IRC.onError());
        });
    }

    static
    {
        try_block:
        try
        {
            File backups = new File("backups");

            if (!backups.exists())
            {
                ServerMain.LOGGER.error("Backups folder does not exist, can't reload any IRC tokens");
                break try_block;
            }

            File ircReconnectionTokenBackups = new File(backups, "irc_reconnection_tokens.txt");

            if (!ircReconnectionTokenBackups.exists())
            {
                ServerMain.LOGGER.error("Saved IRC tokens file does not exist, can't reload any IRC tokens");
                break try_block;
            }

            int count = 0;
            try (FileInputStream fis = new FileInputStream(ircReconnectionTokenBackups))
            {
                String content = new String(fis.readAllBytes());

                JsonObject json = JsonParser.parseString(content).getAsJsonObject();

                JsonArray sessions = json.getAsJsonArray("sessions");

                for (JsonElement session : sessions)
                {
                    JsonObject sessionObj = session.getAsJsonObject();

                    String reconnectionToken = sessionObj.get("token").getAsString();
                    IRC.authorizedBackupTokens.add(reconnectionToken);

                    String uid = sessionObj.get("uid").getAsString();
                    String username = sessionObj.get("username").getAsString();
                    String usertype = sessionObj.get("usertype").getAsString();

                    LoaderEndpoint.IRCSession ircSession = new LoaderEndpoint.IRCSession(uid, username, usertype, true);
                    LoaderEndpoint.awaitingTokens.put(reconnectionToken, ircSession);
                    count++;
                }
            }

            ServerMain.LOGGER.info("Restored IRC connections for {} user(s)", count);
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error("Couldn't reload any IRC tokens: ", t);
        }
    }
}
