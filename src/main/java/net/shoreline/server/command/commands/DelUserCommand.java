package net.shoreline.server.command.commands;

import net.shoreline.server.ServerMain;
import net.shoreline.server.command.Command;
import net.shoreline.server.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class DelUserCommand extends Command
{
    @Override
    public void execute(String[] args,
                        boolean askConfirm) throws Throwable
    {
        if (args.length != 1)
        {
            throw new IllegalStateException("Usage: " + getUsage());
        }

        String username = args[0];

        Database database = ServerMain.getUserDatabase();
        database.getLock().lock(); // lock the connection
        try (Connection connection = database.getDataSource().getConnection())
        {
            int id;
            String findId = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(findId))
            {
                preparedStatement.setString(1, username);

                try (ResultSet resultSet = preparedStatement.executeQuery())
                {
                    if (resultSet.next())
                    {
                        id = resultSet.getInt("id");
                    } else
                    {
                        ServerMain.LOGGER.error("User {} does not exist", username);
                        return;
                    }
                } catch (Throwable t)
                {
                    ServerMain.LOGGER.error("Something went wrong executing the command:", t);
                    return;
                }
            }

            if (askConfirm)
            {
                String message = "Found 1 user that will be affected.";
                if (!askForConfirmation(message))
                {
                    return;
                }
            }

            String resetHwid = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(resetHwid))
            {
                preparedStatement.setInt(1, id);

                try
                {
                    preparedStatement.executeUpdate();
                    ServerMain.LOGGER.info("Deleted user {} from the database.", username);
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
        return "deluser <name>";
    }

    @Override
    public String getDescription()
    {
        return "Deletes a user from the database.";
    }
}
