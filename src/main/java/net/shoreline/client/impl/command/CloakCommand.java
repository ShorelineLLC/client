package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.client.CPacketCloak;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.loader.Loader;

public final class CloakCommand extends Command
{
    private final String userType;

    public CloakCommand()
    {
        super("Cloak", "Cloaks your online user rank", literal("cloak"));
        this.userType = Loader.SESSION.getUserType();
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(
                argument("rank", StringArgumentType.greedyString())
                        .suggests(suggest(getSuggestions()))
                        .executes(c ->
                        {
                            String argument = c.getArgument("rank", String.class);

                            CPacketCloak cloak = new CPacketCloak(argument);
                            IRCManager.getInstance().sendPacket(cloak);

                            return 1;
                        })
        ).executes(c ->
        {
            ChatUtil.error("Invalid usage! Usage: " + getUsage());
            return 1;
        });
    }

    public String[] getSuggestions()
    {
        return switch (this.userType)
        {
            case "dev" -> new String[] { "dev", "beta", "release" };
            case "beta" -> new String[] { "beta", "release" };
            default -> throw new IllegalStateException("unauthorized");
        };
    }
}
