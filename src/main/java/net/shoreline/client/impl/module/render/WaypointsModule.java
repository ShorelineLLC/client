package net.shoreline.client.impl.module.render;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.api.waypoint.Waypoint;
import net.shoreline.client.impl.event.ScreenOpenEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.event.StageEvent;
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
    DecimalFormat format = new DecimalFormat("0.0");
    private final Set<EntryPos> entries = new ConcurrentSet<>();

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
        entries.clear();
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE || !logoutsConfig.getValue())
        {
            return;
        }
        for (EntryPos pos : entries)
        {
            PlayerListEntry entry = pos.entry();
            if (mc.getNetworkHandler().getPlayerList().stream().noneMatch(e -> e.getProfile().getName().equals(entry.getProfile().getName())))
            {
                entries.removeIf(e -> e.entry.getProfile().getName().equals(entry.getProfile().getName()));
                PlayerEntity player = pos.player();
                String serverIp = mc.isInSingleplayer() ? "Singleplayer" : Managers.NETWORK.getServerIp();
                String nametag = String.format("%s Logout" + (coordsConfig.getValue() ? " XYZ %s %s %s" : ""), entry.getProfile().getName(), format.format(player.getX()), format.format(player.getY()), format.format(player.getZ()));
                nametag = (Managers.SOCIAL.isFriend(entry.getProfile().getName()) ? "§b" : "§7") + nametag;
                Managers.WAYPOINT.register(new Waypoint(nametag, serverIp, player.getX(), player.getY(), player.getZ()));
            }
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
    }

    @EventListener
    public void onRemoveEntity(ScreenOpenEvent event)
    {
        if (event.getScreen() instanceof DeathScreen && deathsConfig.getValue())
        {
            String serverIp = mc.isInSingleplayer() ? "Singleplayer" : Managers.NETWORK.getServerIp();
            Managers.WAYPOINT.removeContains("Last Death");
            Managers.WAYPOINT.register(new Waypoint(String.format("§7Last Death" + (coordsConfig.getValue() ? " XYZ %s %s %s" : ""), format.format(mc.player.getX()), format.format(mc.player.getY()), format.format(mc.player.getZ())), serverIp,
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
        for (Waypoint waypoint : Managers.WAYPOINT.getWaypoints())
        {
            if (!waypoint.getIp().equalsIgnoreCase(Managers.NETWORK.getServerIp()) || mc.world.getRegistryKey() != waypoint.getDimension())
            {
                continue;
            }
            Box waypointBox = EntityDimensions.fixed(0.6f, 2.2f).getBoxAt(waypoint.getPos());
            double center = (waypointBox.maxX - waypointBox.minX) / 2.0f;
            RenderManager.renderBoundingBox(event.getMatrices(), waypointBox, 1.5f, ColorsModule.getInstance().getRGB(255));
            RenderManager.renderSign(waypoint.getName(), waypointBox.minX + center, waypointBox.maxY + 0.4, waypointBox.minZ + center, -1);
        }
    }

    public boolean getCoords()
    {
        return coordsConfig.getValue();
    }

    private record EntryPos(PlayerListEntry entry, PlayerEntity player) {}
}
