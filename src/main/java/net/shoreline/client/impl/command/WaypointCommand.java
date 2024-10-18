package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.waypoint.UserWaypoint;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.client.util.world.DimensionUtil;

public class WaypointCommand extends Command
{
    /**
     *
     */
    public WaypointCommand()
    {
        super("Waypoint", "Adds/Removes a waypoint", literal("waypoint"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("add/del", StringArgumentType.string()).suggests(suggest("add", "del", "delete", "remove"))
                .then(argument("waypoint", StringArgumentType.string())
                        .then(argument("x", DoubleArgumentType.doubleArg()).then(argument("y", DoubleArgumentType.doubleArg()).then(argument("z", DoubleArgumentType.doubleArg())
                                .executes(c ->
                                {
                                    String waypointName = StringArgumentType.getString(c, "waypoint");
                                    double x = DoubleArgumentType.getDouble(c, "x");
                                    double y = DoubleArgumentType.getDouble(c, "y");
                                    double z = DoubleArgumentType.getDouble(c, "z");
                                    final String action = StringArgumentType.getString(c, "add/del");
                                    if (action.equalsIgnoreCase("add"))
                                    {
                                        if (Managers.WAYPOINT.contains(waypointName))
                                        {
                                            ChatUtil.error("Waypoint already exist!");
                                            return 0;
                                        }
                                        ChatUtil.clientSendMessage("Added waypoint with name §s" + waypointName);
                                        Managers.WAYPOINT.register(new UserWaypoint(waypointName, mc.isInSingleplayer() ? "Singleplayer" : Managers.NETWORK.getServerIp(), DimensionUtil.getDimension(), x, y, z));
                                    }
                                    return 1;
                                }))))
                        .executes(c ->
                        {
                            String waypointName = StringArgumentType.getString(c, "waypoint");
                            final String action = StringArgumentType.getString(c, "add/del");
                            if (action.equalsIgnoreCase("remove") || action.equalsIgnoreCase("del") || action.equalsIgnoreCase("delete"))
                            {
                                if (!Managers.WAYPOINT.contains(waypointName))
                                {
                                    ChatUtil.error("Waypoint does not exist!");
                                    return 0;
                                }
                                ChatUtil.clientSendMessage("Removed waypoint with name §c" + waypointName);
                                Managers.WAYPOINT.remove(waypointName);
                            }
                            return 1;
                        })).executes(c ->
                {
                    ChatUtil.error("Must provide waypoint name!");
                    return 1;
                })).executes(c ->
        {
            ChatUtil.error("Invalid usage! Usage: " + getUsage());
            return 1;
        });
    }
}
