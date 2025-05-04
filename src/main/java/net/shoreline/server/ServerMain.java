package net.shoreline.server;

import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.command.CommandManager;
import net.shoreline.server.database.Database;
import net.shoreline.server.discord.DiscordBot;
import net.shoreline.server.route.frontend.FrontendEndpoint;
import net.shoreline.server.route.installer.InstallerEndpoint;
import net.shoreline.server.route.irc.IRCEndpoint;
import net.shoreline.server.route.loader.LoaderEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerMain
{
    public static final Logger LOGGER = LoggerFactory.getLogger("Server");

    private static final Database USER_DATABASE = new Database(
            "jdbc:mysql://172.18.0.1:3306/s1_users",
            "u1_Fst3SEAPVf",
            "y60I3=lhoPGXkeohxTVB.7HC"
    );

    public static void main(String[] args)
    {
        Javalin API = Javalin.create(config ->
        {
            config.router
                    .apiBuilder(new LoaderEndpoint());
        }).start(1337);

        Javalin FRONTEND = Javalin.create(config ->
        {
            config.router
                    .apiBuilder(new FrontendEndpoint());
        }).start(1338);

        Javalin IRC = Javalin.create(config ->
        {
            config.router
                    .apiBuilder(new IRCEndpoint());
        }).start(1339);

        Javalin INSTALLER = Javalin.create(config ->
        {
            config.router
                    .apiBuilder(new InstallerEndpoint());
        }).start(1340);

        // Default responses
        API.get("/*", ctx ->
        {
            throw new NotFoundResponse();
        });

        FRONTEND.get("/*", ctx ->
        {
            throw new NotFoundResponse();
        });

        IRC.get("/*", ctx ->
        {
            throw new NotFoundResponse();
        });

        INSTALLER.get("/*", ctx ->
        {
            throw new NotFoundResponse();
        });

        LOGGER.info("Launching discord bot...");
        try
        {
            DiscordBot.loadBot();
        } catch (Throwable t)
        {
            LOGGER.error("Failed to load the discord bot: ", t);
            throw new RuntimeException(t);
        }
        LOGGER.info("Bot launched");

        CommandManager.startListening();
    }

    public static Database getUserDatabase()
    {
        return USER_DATABASE;
    }
}
