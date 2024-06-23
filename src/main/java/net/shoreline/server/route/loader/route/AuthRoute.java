package net.shoreline.server.route.loader.route;

import io.javalin.http.*;
import net.shoreline.server.ServerMain;
import net.shoreline.server.route.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Throws:
 *   NotFoundResponse (404) -> User agent not set properly, hardware ID not set properly
 *   UnauthorizedResponse (401) -> Hardware ID not found on server
 *   InternalServerErrorResponse (500) -> Some internal error happened
 */
public final class AuthRoute extends Route
{
    @Override
    public void doHandle(Context context)
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-client".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String hardwareID = context.header("Hardware-ID");

        if (hardwareID == null)
        {
            throw new NotFoundResponse();
        }

        try (Connection connection = ServerMain.getUserDatabase().getDataSource().getConnection())
        {
            String query = "SELECT u.username, u.uid, u.usertype FROM users u JOIN hwids h ON u.id = h.user_id WHERE h.hwid = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query))
            {
                preparedStatement.setString(1, hardwareID);
                try (ResultSet resultSet = preparedStatement.executeQuery())
                {
                    if (resultSet.next())
                    {
                        String username = resultSet.getString("username");
                        String uid = resultSet.getString("uid");
                        String usertype = resultSet.getString("usertype");

                        context.sessionAttribute("Hardware-ID", hardwareID);
                        context.sessionAttribute("Username", username);
                        context.sessionAttribute("UID", uid);
                        context.sessionAttribute("Usertype", usertype);

                        context.result(hardwareID + ":" + username + ":" + uid + ":" + usertype);
                    } else
                    {
                        ServerMain.LOGGER.info(hardwareID);
                        throw new UnauthorizedResponse();
                    }
                } catch (Throwable t)
                {
                    ServerMain.LOGGER.error("Failed to execute query: ", t);
                    throw new InternalServerErrorResponse();
                }
            } catch (Throwable t)
            {
                ServerMain.LOGGER.error("Failed to prepare statement: ", t);
                throw new InternalServerErrorResponse();
            }
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error("Failed to establish user database connection: ", t);
            throw new InternalServerErrorResponse();
        }
    }
}
