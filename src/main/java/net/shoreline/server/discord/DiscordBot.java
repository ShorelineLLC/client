package net.shoreline.server.discord;

import com.mysql.cj.protocol.MessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.shoreline.server.ServerMain;
import net.shoreline.server.discord.command.CommandListener;

import java.util.EnumSet;

public final class DiscordBot
{
    public static String BOT_TOKEN = "MTI3NzE2MDkwMjc4MDc4MDU5NQ.G0vtRF.cNlvNxybE4W-NcUOKBEL1Zhafmu3Hu_S0RbVQo";

    public static Guild SHORELINE_GUILD;

    public static void loadBot() throws Throwable
    {
        JDALogger.setFallbackLoggerEnabled(false);

        JDABuilder builder = JDABuilder.createLight(
                BOT_TOKEN,
                EnumSet.of(
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS
                )
        );

        builder.addEventListeners(CommandListener.getInstance());

        JDA jda = builder.build();
        jda.awaitReady();

        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("purchase", "Sends the url to the purchase the client.").setGuildOnly(true),
                Commands.slash("tos", "Sends the client terms of service.").setGuildOnly(true),
                Commands.slash("resethwid", "Resets a user's HWID.")
                        .addOption(OptionType.USER, "user", "The user to reset", true)
        );
        commands.queue();

        SHORELINE_GUILD = jda.getGuildById(1216718926134906900L);

        if (SHORELINE_GUILD == null)
        {
            throw new IllegalStateException("Couldn't find Shoreline's Guild");
        }

        ServerMain.LOGGER.info("Located Shoreline Guild");
    }
}
