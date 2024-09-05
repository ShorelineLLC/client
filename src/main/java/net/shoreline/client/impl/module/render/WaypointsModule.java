package net.shoreline.client.impl.module.render;

import com.google.common.collect.Sets;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.api.waypoint.Waypoint;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.world.LoadWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.world.DimensionUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.text.DecimalFormat;
import java.util.Set;

/**
 * @author linus
 * @since 1.0
 */
public class WaypointsModule extends ToggleModule
{
    private static WaypointsModule INSTANCE;

    Config<Boolean> logoutsConfig = register(new BooleanConfig("LogoutPoints", "Marks the position of player logouts", false));
    Config<Boolean> deathsConfig = register(new BooleanConfig("DeathPoints", "Marks the position of player deaths", false));
    Config<Boolean> coordsConfig = register(new BooleanConfig("Coords", "Shows the coordinates of the waypoint", true));
    Config<Boolean> distanceConfig = register(new BooleanConfig("Distance", "Shows the distance to the waypoint", true));
    DecimalFormat format = new DecimalFormat("0.0");
    private final Set<EntryPos> entries = Sets.newConcurrentHashSet();

    public WaypointsModule()
    {
        super("Waypoints", "Renders a waypoint at marked locations", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    public static WaypointsModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onDisable()
    {
        Managers.WAYPOINT.clear();
        entries.clear();
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        onDisable();
    }

    @EventListener
    public void onLoadWorld(LoadWorldEvent event)
    {
        onDisable();
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (!logoutsConfig.getValue() || mc.getNetworkHandler() == null)
        {
            return;
        }
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList())
        {
            if (entries.stream().noneMatch(e -> e.entry.getProfile().getName().equals(entry.getProfile().getName())))
            {
                PlayerEntity player = mc.world.getPlayerByUuid(entry.getProfile().getId());
                if (player == null)
                {
                    continue;
                }
                entries.add(new EntryPos(entry, player));
                Managers.WAYPOINT.removeContains(entry.getProfile().getName());
            }
        }
        for (EntryPos pos : entries)
        {
            PlayerListEntry entry = pos.entry();
            if (mc.getNetworkHandler().getPlayerList().stream().noneMatch(e -> e.getProfile().getName().equals(entry.getProfile().getName())))
            {
                entries.removeIf(e -> e.entry.getProfile().getName().equals(entry.getProfile().getName()));
                PlayerEntity player = pos.player();
                String serverIp = mc.isInSingleplayer() ? "Singleplayer" : Managers.NETWORK.getServerIp();
                Managers.WAYPOINT.register(new Waypoint(player.getGameProfile().getName() + "'s Logout", serverIp, DimensionUtil.getDimension(), player.getX(), player.getY(), player.getZ()));
            }
        }
    }

    @EventListener
    public void onRemoveEntity(EntityDeathEvent event)
    {
        if (event.getEntity() instanceof ClientPlayerEntity && deathsConfig.getValue())
        {
            String serverIp = mc.isInSingleplayer() ? "Singleplayer" : Managers.NETWORK.getServerIp();
            Managers.WAYPOINT.removeContains("Last Death");
            Managers.WAYPOINT.register(new Waypoint("Last Death", serverIp, DimensionUtil.getDimension(),
                    mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        RenderBuffers.preRender();
        for (Waypoint waypoint : Managers.WAYPOINT.getWaypoints())
        {
            if (!waypoint.getIp().equalsIgnoreCase(mc.isInSingleplayer() ? "Singleplayer" : Managers.NETWORK.getServerIp()) || DimensionUtil.getDimension() != waypoint.getDimension())
            {
                continue;
            }
            Box waypointBox = EntityDimensions.fixed(0.6f, 2.2f).getBoxAt(waypoint.getPos());
            double center = (waypointBox.maxX - waypointBox.minX) / 2.0f;
            RenderManager.renderBoundingBox(event.getMatrices(), waypointBox, 1.5f, ColorsModule.getInstance().getRGB(255));
            int dist = (int) Math.sqrt(mc.player.squaredDistanceTo(waypoint.getX(), waypoint.getY(), waypoint.getZ()));
            String waypointTag = "§7" + waypoint.getName() + (coordsConfig.getValue() ? String.format(" XYZ %s %s %s", format.format(waypoint.getX()), format.format(waypoint.getY()), format.format(waypoint.getZ())) : "") + (distanceConfig.getValue() ? String.format(" %sm", dist) : "");
            RenderManager.renderSign(waypointTag, waypointBox.minX + center, waypointBox.maxY + 0.4, waypointBox.minZ + center, -1);
        }
        RenderBuffers.postRender();
    }

    private record EntryPos(PlayerListEntry entry, PlayerEntity player)
    {

    }
}
