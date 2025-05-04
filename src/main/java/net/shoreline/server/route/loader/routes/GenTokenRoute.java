package net.shoreline.server.route.loader.routes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.javalin.http.*;
import net.shoreline.server.ServerMain;
import net.shoreline.server.database.Database;
import net.shoreline.server.encryption.Encryption;
import net.shoreline.server.route.Route;
import net.shoreline.server.route.loader.LoaderEndpoint;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Throws:
 *   NotFoundResponse (404) -> User agent not set properly, session attribute not set, invalid json body
 *   NotAcceptableResponse (406) -> Loader hash does not match that on the server (it's been tampered with)
 *   InternalServerErrorResponse (500) -> Some internal error happened
 */

public final class GenTokenRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-client".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String username = context.sessionAttribute("Username");

        if (username == null)
        {
            throw new NotFoundResponse();
        }

        String usertype = context.sessionAttribute("User-Type");

        if (usertype == null)
        {
            throw new NotFoundResponse();
        }

        String loaderHash;
        try
        {
            JsonObject request = JsonParser.parseString(context.body()).getAsJsonObject();

            loaderHash = request.get("Loader-Hash").getAsString();
        } catch (Throwable t)
        {
            throw new NotFoundResponse();
        }

        String path = String.format(
                "/home/container/assets/loader/%s/loader.jar",
                usertype
        );

        String encrypted;

        try
        {
            byte[] bytes = Files.readAllBytes(Paths.get(path));

            String unencrypted = Arrays.toString(bytes);
            encrypted = Encryption.encrypt(unencrypted);
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error("Failed to complete integrity check: ", t);
            throw new InternalServerErrorResponse();
        }


        if (!encrypted.equals(loaderHash))
        {
            if (usertype.equals("dev"))
            {
                ServerMain.LOGGER.info("Allowing {} to launch from a malformed loader due to dev status", username);
            } else
            {
                // Hash doesn't match, ban time
                ServerMain.LOGGER.info("[ALERT] {}'s loader does not match the expected hash, auto-banning...", username);
                Database database = ServerMain.getUserDatabase();
                database.getLock().lock(); // lock the connection
                try (Connection connection = database.getDataSource().getConnection())
                {
                    String banSql = "UPDATE users SET banned = 1 WHERE username = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(banSql))
                    {
                        preparedStatement.setString(1, username);

                        try
                        {
                            int affectedRows = preparedStatement.executeUpdate();

                            if (affectedRows == 0)
                            {
                                ServerMain.LOGGER.error("Tried to ban an already banned user {}, this should never happen", username);
                                throw new IllegalStateException();
                            }

                            ServerMain.LOGGER.warn("Auto-banned {} for a malformed loader.", username);
                        } catch (Throwable t)
                        {
                            throw new InternalServerErrorResponse();
                        }
                    } catch (Throwable t)
                    {
                        if (t instanceof HttpResponseException)
                        {
                            throw t;
                        }

                        ServerMain.LOGGER.error("Failed to prepare statement: ", t);
                        throw new InternalServerErrorResponse();
                    }
                } catch (Throwable t)
                {
                    if (t instanceof HttpResponseException)
                    {
                        throw t;
                    }

                    ServerMain.LOGGER.error("Failed to establish user database connection: ", t);
                    throw new InternalServerErrorResponse();
                } finally
                {
                    database.getLock().unlock();
                }

                throw new NotAcceptableResponse();
            }
        }

        String token = switch (usertype)
        {
            case "dev" -> LoaderEndpoint.DEV_TOKEN_MANAGER.getAndAuthorizeNewToken();
            case "beta" -> LoaderEndpoint.BETA_TOKEN_MANAGER.getAndAuthorizeNewToken();
            case "release" -> LoaderEndpoint.RELEASE_TOKEN_MANAGER.getAndAuthorizeNewToken();
            default ->
            {
                ServerMain.LOGGER.error("Unrecognized session usertype: {}", usertype);
                throw new InternalServerErrorResponse();
            }
        };

        JsonObject response = new JsonObject();

        response.add("Token", new JsonPrimitive(token));

        context.result(response.toString()).contentType("application/json");
    }
}
