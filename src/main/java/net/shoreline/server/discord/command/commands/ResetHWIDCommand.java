package net.shoreline.server.discord.command.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.shoreline.server.ServerMain;
import net.shoreline.server.database.Database;
import net.shoreline.server.discord.command.Command;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class ResetHWIDCommand extends Command
{
    public ResetHWIDCommand()
    {
        super("resethwid");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event,
                        EmbedBuilder builder)
    {
        Member member = event.getMember();

        if (member == null || !event.getMember().hasPermission(Permission.ADMINISTRATOR))
        {
            builder.setDescription("You do not have permission to execute this command.");
            builder.setColor(Color.RED);
            return;
        }

        OptionMapping userOption = event.getOption("user");

        if (userOption == null)
        {
            builder.setDescription("Please specify a user.");
            builder.setColor(Color.RED);
            return;
        }

        String userDiscord = userOption.getAsUser().getName();

        try
        {
            Database database = ServerMain.getUserDatabase();
            database.getLock().lock(); // lock the connection
            try (Connection connection = database.getDataSource().getConnection())
            {
                int id;
                String username;
                String findId = "SELECT id, username FROM users WHERE discord = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(findId))
                {
                    preparedStatement.setString(1, userDiscord);

                    try (ResultSet resultSet = preparedStatement.executeQuery())
                    {
                        if (resultSet.next())
                        {
                            id = resultSet.getInt("id");
                            username = resultSet.getString("username");
                        } else
                        {
                            builder.setDescription("This user is not linked to a Shoreline account.");
                            builder.setColor(Color.RED);
                            return;
                        }
                    } catch (Throwable t)
                    {
                        ServerMain.LOGGER.error("Something went wrong executing the command:", t);
                        builder.setDescription("Internal error. Stacktrace printed in server console.");
                        builder.setColor(Color.RED);
                        return;
                    }
                }

                String resetHwid = "DELETE FROM hwids WHERE user_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(resetHwid))
                {
                    preparedStatement.setInt(1, id);

                    try
                    {
                        int rowsAffected = preparedStatement.executeUpdate();
                        builder.setDescription("Reset " + rowsAffected + " associated HWID(s) for user " + username);
                        builder.setColor(new Color(39, 127, 196));
                    } catch (Throwable t)
                    {
                        ServerMain.LOGGER.error("Something went wrong executing the command:", t);
                        builder.setDescription("Internal error. Stacktrace printed in server console.");
                        builder.setColor(Color.RED);
                    }
                }
            } finally
            {
                database.getLock().unlock();
            }
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error("Reset error:", t);
            builder.setDescription("Internal error. Stacktrace printed in server console.");
            builder.setColor(Color.RED);
        }
    }
}
