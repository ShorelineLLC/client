package net.shoreline.server.command.commands;

import net.shoreline.server.ServerMain;
import net.shoreline.server.command.Command;
import net.shoreline.server.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class FindUserCommand extends Command
{
    @Override
    public void execute(String[] args,
                        boolean askConfirm) throws Throwable
    {
        if (args.length != 1)
        {
            throw new IllegalStateException("Usage: " + getUsage());
        }

        String hwid = args[0];

        Database database = ServerMain.getUserDatabase();
        database.getLock().lock(); // lock the connection
        try (Connection connection = database.getDataSource().getConnection())
        {
            int id;
            String findId = "SELECT user_id FROM hwids WHERE hwid = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(findId))
            {
                preparedStatement.setString(1, hwid);

                try (ResultSet resultSet = preparedStatement.executeQuery())
                {
                    if (resultSet.next())
                    {
                        id = resultSet.getInt("user_id");
                    } else
                    {
                        ServerMain.LOGGER.error("Couldn't find an associated hwid {} with any accounts.", hwid);
                        return;
                    }
                } catch (Throwable t)
                {
                    ServerMain.LOGGER.error("Something went wrong executing the command:", t);
                    return;
                }
            }

            String findUser = "SELECT username FROM users WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(findUser))
            {
                preparedStatement.setInt(1, id);

                try(ResultSet resultSet = preparedStatement.executeQuery())
                {
                    if (resultSet.next())
                    {
                        String username = resultSet.getString("username");
                        ServerMain.LOGGER.info("The specified HWID is linked to {}.", username);
                    } else
                    {
                        ServerMain.LOGGER.error("Found a registered hwid, but can't find any accounts associated with it.\n" +
                                "Something very wrong must have happened.");
                    }
                } catch (Throwable t)
                {
                    ServerMain.LOGGER.error("Something went wrong executing the command:", t);
                }
            }
        } finally
        {
            database.getLock().unlock();
        }
    }

    @Override
    public String getUsage()
    {
        return "finduser <hwid>";
    }

    @Override
    public String getDescription()
    {
        return "Returns the user associated with an HWID.";
    }
}
