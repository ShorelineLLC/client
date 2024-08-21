package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.PlayerArgumentType;
import net.shoreline.client.util.chat.ChatUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.text.DecimalFormat;

public class CoordsCommand extends Command
{
    public CoordsCommand()
    {
        super("Coords", "Sends player coordinates", literal("coords"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("player", PlayerArgumentType.player()).executes(c ->
        {
            String playerName = PlayerArgumentType.getPlayer(c, "player");
            ChatUtil.serverSendCommand("w " + playerName + " " + getCoords());
            return 1;
        })).executes(c ->
        {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = new StringSelection(getCoords());
            clipboard.setContents(transferable, null);
            ChatUtil.clientSendMessage("Copied player coordinates to clipboard!");
            return 1;
        });
    }

    public String getCoords()
    {
        DecimalFormat decimal = new DecimalFormat("0.0");
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        return decimal.format(x) + ", " + decimal.format(y) + ", " + decimal.format(z);
    }
}
