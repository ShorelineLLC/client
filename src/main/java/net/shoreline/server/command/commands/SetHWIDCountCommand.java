package net.shoreline.server.command.commands;

import net.shoreline.server.ServerMain;
import net.shoreline.server.command.Command;
import net.shoreline.server.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class SetHWIDCountCommand extends Command
{
    @Override
    public void execute(String[] args,
                        boolean askConfirm) throws Throwable
    {
        if (args.length != 2)
        {
            throw new IllegalStateException("Usage: " + getUsage());
        }

        String username = args[0];

        int newHwidCount;
        try
        {
            newHwidCount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e)
        {
            throw new IllegalStateException("Usage: " + getUsage());
        }

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

            String hwidCountSql = "SELECT max_hwids FROM users WHERE id = ?";
            try (PreparedStatement hwidCountStmt = connection.prepareStatement(hwidCountSql))
            {
                hwidCountStmt.setInt(1, id);

                int maxHwids;
                try (ResultSet countResultSet = hwidCountStmt.executeQuery())
                {
                    if (countResultSet.next())
                    {
                        maxHwids = countResultSet.getInt("max_hwids");
                    } else
                    {
                        ServerMain.LOGGER.error("Couldn't query the max_hwids column from the database. This shouldn't happen.");
                        return;
                    }
                }

                if (maxHwids == newHwidCount)
                {
                    ServerMain.LOGGER.info("User {} already has {} registered HWID(s).", username, maxHwids);
                    return;
                }

                connection.setAutoCommit(false);
                String addHwidSql = "UPDATE users SET max_hwids = ? WHERE id = ?";
                try (PreparedStatement addHwidStmt = connection.prepareStatement(addHwidSql))
                {
                    addHwidStmt.setInt(1, newHwidCount);
                    addHwidStmt.setInt(2, id);
                    addHwidStmt.executeUpdate();
                    connection.commit();

                    if (maxHwids > newHwidCount)
                    {
                        ServerMain.LOGGER.info("Decreased the max HWID count for user {} from {} to {}.", username, maxHwids, newHwidCount);
                        ServerMain.LOGGER.info("This will not remove any current HWIDs over the max count. Use resethwid to reset them all.");
                    } else
                    {
                        ServerMain.LOGGER.info("Increased the max HWID count for user {} from {} to {}.", username, maxHwids, newHwidCount);
                    }
                } catch (SQLException e)
                {
                    connection.rollback();
                    ServerMain.LOGGER.error("Couldn't execute the update: ", e);
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
        return "sethwidcount <username> <hwidcount>";
    }

    @Override
    public String getDescription()
    {
        return "Sets a users allowed number of HWIDs (default 1)";
    }
}
