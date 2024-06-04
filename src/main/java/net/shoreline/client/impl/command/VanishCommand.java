package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.mixin.accessor.AccessorEntity;
import net.shoreline.client.util.chat.ChatUtil;

/**
 * @author linus
 * @since 1.0
 */
public class VanishCommand extends Command
{
    private Entity mount;

    public VanishCommand()
    {
        super("Vanish", "Desyncs the riding entity", literal("vanish"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("mount", StringArgumentType.string())
                .suggests(suggest("mount", "remount")).executes(c ->
                {
                    String dismount = StringArgumentType.getString(c, "mount");
                    if (dismount.equalsIgnoreCase("dismount"))
                    {
                        if (mc.player.isRiding() && mc.player.getVehicle() != null)
                        {
                            if (mount != null)
                            {
                                ChatUtil.error("Entity vanished, must remount before mounting!");
                                return 0;
                            }
                            mount = mc.player.getVehicle();
                            mc.player.dismountVehicle();
                            mc.world.removeEntity(mount.getId(), Entity.RemovalReason.DISCARDED);
                        }
                    }
                    else if (dismount.equalsIgnoreCase("remount"))
                    {
                        if (mount == null)
                        {
                            ChatUtil.error("No vanished entity!");
                            return 0;
                        }
                        //
                        ((AccessorEntity) mount).hookUnsetRemoved();
                        mc.world.addEntity(mount);
                        mc.player.startRiding(mount, true);
                        mount = null;
                    }
                    return 1;
                })).executes(c ->
        {
            ChatUtil.error("Invalid usage! Usage: " + getUsage());
            return 1;
        });
    }
}
