package net.shoreline.server.route.installer.routes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.*;
import net.shoreline.server.ServerMain;
import net.shoreline.server.database.Database;
import net.shoreline.server.discord.role.RoleManager;
import net.shoreline.server.route.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Pattern;

/**
 * Throws:
 *   NotFoundResponse (404) ->
 *      1) If the user agent doesn't match
 *   UnauthorizedResponse (401) ->
 *      1) If the user doesn't have an active session (aka, they accessed this route without logging in)
 *   NotAcceptableResponse (406) ->
 *      1) If the discord username provided is not valid
 *   InternalServerErrorResponse (500) ->
 *      1) If something went wrong here
 */
public final class LinkDiscordRoute extends Route
{
    private final Pattern DISCORD_PATTERN = Pattern.compile("^(?!.*?\\.{2,})[a-z0-9_\\.]{2,32}$");

    @Override
    public void doHandle(Context context) throws Exception
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-installer".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String username = context.sessionAttribute("Username");

        if (username == null)
        {
            throw new UnauthorizedResponse();
        }

        String contentType = context.contentType();

        if (!"application/json".equals(contentType))
        {
            throw new NotFoundResponse();
        }

        String discordUsername;
        try
        {
            JsonObject request = JsonParser.parseString(context.body()).getAsJsonObject();

            discordUsername = request.get("Username").getAsString();
        } catch (Throwable t)
        {
            throw new NotFoundResponse();
        }

        if (!DISCORD_PATTERN.matcher(discordUsername).matches())
        {
            throw new NotAcceptableResponse();
        }

        try
        {
            Database database = ServerMain.getUserDatabase();
            database.getLock().lock(); // lock the connection

            try (Connection connection = database.getDataSource().getConnection())
            {
                // Check if the user already has a discord linked
                boolean userAlreadyHasLinked = false;

                String findUserDiscord = "SELECT discord FROM users WHERE username = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(findUserDiscord))
                {
                    preparedStatement.setString(1, username);

                    try (ResultSet resultSet = preparedStatement.executeQuery())
                    {
                        if (resultSet.next())
                        {
                            String linkedDiscord = resultSet.getString("discord");
                            if (linkedDiscord != null && !linkedDiscord.isEmpty())
                            {
                                userAlreadyHasLinked = true;
                            }
                        }
                    } catch (Throwable t)
                    {
                        throw new InternalServerErrorResponse();
                    }
                }

                if (userAlreadyHasLinked)
                {
                    JsonObject response = new JsonObject();

                    response.addProperty("Success", false);
                    response.addProperty("Error", "You already have a Discord account linked.");

                    context.header("Content-Type", "application/json");
                    context.result(response.toString());
                    return;
                }

                // Check if there is a user who already has this discord
                boolean discordAlreadyInUse = false;
                String findDiscord = "SELECT * FROM users WHERE discord = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(findDiscord))
                {
                    preparedStatement.setString(1, discordUsername);

                    try (ResultSet resultSet = preparedStatement.executeQuery())
                    {
                        if (resultSet.next())
                        {
                            discordAlreadyInUse = true;
                        }
                    } catch (Throwable t)
                    {
                        throw new InternalServerErrorResponse();
                    }
                }

                if (discordAlreadyInUse)
                {
                    JsonObject response = new JsonObject();

                    response.addProperty("Success", false);
                    response.addProperty("Error", "This Discord username is linked to another user.");

                    context.header("Content-Type", "application/json");
                    context.result(response.toString());
                    return;
                }

                boolean result = RoleManager.addRole(discordUsername, "user");

                if (result)
                {
                    String updateDiscord = "UPDATE users SET discord = ? WHERE username = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(updateDiscord))
                    {
                        preparedStatement.setString(1, discordUsername);
                        preparedStatement.setString(2, username);

                        try
                        {
                            int affectedRows = preparedStatement.executeUpdate();

                            if (affectedRows == 0)
                            {
                                ServerMain.LOGGER.error("Successfully linked a user's Discord, but couldn't update it " +
                                        "in the database. This should never happen");
                                throw new IllegalStateException();
                            }

                            ServerMain.LOGGER.info("{} linked their Discord ({}) through the installer.", username, discordUsername);
                        } catch (Throwable t)
                        {
                            throw new InternalServerErrorResponse();
                        }
                    }
                }

                JsonObject response = new JsonObject();

                response.addProperty("Success", result);
                response.addProperty("Error", "Failed to link your Discord. Did you type your username correctly?");

                context.header("Content-Type", "application/json");
                context.result(response.toString());
            } finally
            {
                database.getLock().unlock();
            }
        } catch (Throwable t)
        {
            throw new InternalServerErrorResponse();
        }
    }
}
