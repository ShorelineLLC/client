package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.util.chat.ChatUtil;

public class YawCommand extends Command
{
    public YawCommand()
    {
        super("Yaw", "Sets the player yaw", literal("yaw"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("yaw", FloatArgumentType.floatArg()).executes(c ->
        {
            float yaw = FloatArgumentType.getFloat(c, "yaw");
            mc.player.setYaw(yaw);
            mc.player.setHeadYaw(yaw);
            mc.player.setBodyYaw(yaw);
            ChatUtil.clientSendMessage("Set player yaw to §s" + yaw);
            return 1;
        }));
    }
}
