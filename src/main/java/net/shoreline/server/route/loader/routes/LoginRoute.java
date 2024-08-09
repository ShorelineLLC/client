package net.shoreline.server.route.loader.routes;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import io.javalin.http.*;
import net.shoreline.server.ServerMain;
import net.shoreline.server.database.Database;
import net.shoreline.server.route.Route;
import net.shoreline.server.route.loader.LoaderEndpoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class LoginRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-client".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String contentType = context.header("Content-Type");

        if (!"application/json".equals(contentType))
        {
            throw new NotFoundResponse();
        }

        LoginRequest loginRequest;
        try
        {
            loginRequest = new Gson().fromJson(context.body(), LoginRequest.class);
        } catch (Throwable t)
        {
            throw new NotFoundResponse();
        }

        String username = loginRequest.username;
        if (username == null)
        {
            throw new NotFoundResponse();
        }

        String password = loginRequest.password;
        if (password == null)
        {
            throw new NotFoundResponse();
        }

        String hardwareID = loginRequest.hardwareID;
        if (hardwareID == null)
        {
            throw new NotFoundResponse();
        }

        Database database = ServerMain.getUserDatabase();
        database.getLock().lock(); // lock the connection
        try (Connection connection = database.getDataSource().getConnection())
        {
            int id;
            String uid;
            String usertype;
            String findUser = "SELECT id, uid, usertype FROM users WHERE username = ? AND user_password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(findUser))
            {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                try (ResultSet resultSet = preparedStatement.executeQuery())
                {
                    if (resultSet.next())
                    {
                        id = resultSet.getInt("id");
                        uid = resultSet.getString("uid");
                        usertype = resultSet.getString("usertype");
                    } else
                    {
                        throw new UnauthorizedResponse();
                    }
                } catch (Throwable t)
                {
                    if (t instanceof HttpResponseException)
                    {
                        throw t;
                    }

                    ServerMain.LOGGER.error("Failed to execute query: ", t);
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

            String matchHwid = "SELECT hwid FROM hwids WHERE user_id = ?";
            String hwidCountSql = "SELECT COUNT(*) as hwid_count FROM hwids WHERE user_id = ?";
            String maxHwidSql = "SELECT max_hwids FROM users WHERE id = ?";

            try (PreparedStatement hwidCountStmt = connection.prepareStatement(hwidCountSql);
                 PreparedStatement maxHwidStmt = connection.prepareStatement(maxHwidSql);
                 PreparedStatement matchHwidStmt = connection.prepareStatement(matchHwid))
            {
                hwidCountStmt.setInt(1, id);
                maxHwidStmt.setInt(1, id);
                matchHwidStmt.setInt(1, id);

                int hwidCount;
                int maxHwids;

                try (ResultSet countResultSet = hwidCountStmt.executeQuery())
                {
                    if (countResultSet.next())
                    {
                        hwidCount = countResultSet.getInt("hwid_count");
                    } else
                    {
                        throw new UnauthorizedResponse();
                    }
                }

                try (ResultSet maxHwidResultSet = maxHwidStmt.executeQuery())
                {
                    if (maxHwidResultSet.next())
                    {
                        maxHwids = maxHwidResultSet.getInt("max_hwids");
                    } else
                    {
                        throw new UnauthorizedResponse();
                    }
                }

                try (ResultSet resultSet = matchHwidStmt.executeQuery())
                {
                    boolean matchFound = false;

                    while (resultSet.next())
                    {
                        if (resultSet.getString("hwid").equals(hardwareID))
                        {
                            matchFound = true;
                            break;
                        }
                    }

                    if (!matchFound && hwidCount < maxHwids)
                    {
                        if (hwidCount == 0)
                        {
                            ServerMain.LOGGER.info("Authorizing {} for the first time with a hardware ID of {}", username, hardwareID);
                        } else
                        {
                            ServerMain.LOGGER.info("Authorizing {} on a new computer with a hardware ID of {}", username, hardwareID);
                        }

                        connection.setAutoCommit(false);
                        String insertionSql = "INSERT INTO hwids (user_id, hwid) VALUES (?, ?)";
                        try (PreparedStatement prepared = connection.prepareStatement(insertionSql))
                        {
                            prepared.setInt(1, id);
                            prepared.setString(2, hardwareID);
                            prepared.executeUpdate();
                            connection.commit();
                        } catch (SQLException e)
                        {
                            connection.rollback();
                            throw new InternalServerErrorResponse();
                        }

                        matchFound = true;
                    }

                    if (!matchFound)
                    {
                        throw new ForbiddenResponse();
                    }

                    context.sessionAttribute("Hardware-ID", hardwareID);
                    context.sessionAttribute("Username", username);
                    context.sessionAttribute("UID", uid);
                    context.sessionAttribute("User-Type", usertype);

                    String ircToken = LoaderEndpoint.IRC_TOKEN_MANAGER.getAndAuthorizeNewToken();

                    LoaderEndpoint.IRCSession ircSession = new LoaderEndpoint.IRCSession(
                            uid,
                            username,
                            usertype,
                            true
                    );

                    LoaderEndpoint.awaitingTokens.put(ircToken, ircSession);

                    LoginResponse loginResponse = new LoginResponse(
                            hardwareID,
                            username,
                            uid,
                            usertype,
                            ircToken
                    );

                    String jsonResponse = new Gson().toJson(loginResponse);

                    context.header("Content-Type", "application/json");
                    context.result(jsonResponse);
                } catch (Throwable t)
                {
                    if (t instanceof HttpResponseException)
                    {
                        throw t;
                    }

                    ServerMain.LOGGER.error("Failed to execute query: ", t);
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
    }

    private static class LoginRequest
    {
        @SerializedName("Username")
        private String username;

        @SerializedName("Password")
        private String password;

        @SerializedName("Hardware-ID")
        private String hardwareID;
    }

    private static class LoginResponse
    {
        @SerializedName("Hardware-ID")
        private String hwid;

        @SerializedName("Username")
        private String username;

        @SerializedName("UID")
        private String uid;

        @SerializedName("User-Type")
        private String usertype;

        @SerializedName("IRC-Token")
        private String ircToken;

        private LoginResponse(String hwid,
                              String username,
                              String uid,
                              String usertype,
                              String ircToken)
        {
            this.hwid = hwid;
            this.username = username;
            this.uid = uid;
            this.usertype = usertype;
            this.ircToken = ircToken;
        }
    }
}
