package net.shoreline.client.impl.module.render;

import com.google.common.collect.Maps;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
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
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.world.LoadWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.world.DimensionUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;

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

    private final Map<UUID, PlayerEntity> loginPlayers = Maps.newConcurrentMap();
    private final Map<UUID, PlayerEntity> logoutPlayers = Maps.newConcurrentMap();

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
        loginPlayers.clear();
        logoutPlayers.clear();
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
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || !logoutsConfig.getValue())
        {
            return;
        }

        if (event.getPacket() instanceof BundleS2CPacket packet)
        {
            for (Packet<?> packet1 : packet.getPackets())
            {
                handlePlayerListPackets(packet1);
            }
        }

        else
        {
            handlePlayerListPackets(event.getPacket());
        }
    }

    public void handlePlayerListPackets(Packet<?> packet1)
    {
        if (packet1 instanceof PlayerListS2CPacket packet
                && packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER))
        {
            for (PlayerListS2CPacket.Entry player : packet.getPlayerAdditionEntries())
            {
                for (UUID uuid : logoutPlayers.keySet())
                {
                    if (!uuid.equals(player.profile().getId()))
                    {
                        continue;
                    }
                    logoutPlayers.remove(uuid);
                }
            }
            loginPlayers.clear();
        }

        else if (packet1 instanceof PlayerRemoveS2CPacket packet)
        {
            for (UUID uuid2 : packet.profileIds())
            {
                for (UUID uuid : loginPlayers.keySet())
                {
                    if (!uuid.equals(uuid2))
                    {
                        continue;
                    }
                    final PlayerEntity player = loginPlayers.get(uuid);
                    if (!logoutPlayers.containsKey(uuid))
                    {
                        logoutPlayers.put(uuid, player);
                    }
                }
            }
            loginPlayers.clear();
        }
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.POST)
        {
            return;
        }
        for (PlayerEntity player : mc.world.getPlayers())
        {
            if (player == null || player.equals(mc.player))
            {
                continue;
            }
            loginPlayers.put(player.getGameProfile().getId(), player);
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
            renderWaypointBox(event.getMatrices(), waypoint.getPos(), waypoint.getName());
        }

        if (logoutsConfig.getValue())
        {
            for (UUID uuid : logoutPlayers.keySet())
            {
                final PlayerEntity data = logoutPlayers.get(uuid);
                if (data == null)
                {
                    continue;
                }
                renderWaypointBox(event.getMatrices(), data.getPos(),
                        data.getName().getString() + "'s Logout", data.isCrawling());
            }
        }

        RenderBuffers.postRender();
    }

    private void renderWaypointBox(MatrixStack matrixStack, Vec3d pos, String tag)
    {
        renderWaypointBox(matrixStack, pos, tag, false);
    }

    private void renderWaypointBox(MatrixStack matrixStack, Vec3d pos, String tag, boolean crawlHitbox)
    {
        Box waypointBox = crawlHitbox ? EntityDimensions.changing(0.6f, 0.6f).withEyeHeight(0.4f).getBoxAt(pos) : EntityDimensions.fixed(0.6f, 2.2f).getBoxAt(pos);
        double center = (waypointBox.maxX - waypointBox.minX) / 2.0f;
        RenderManager.renderBoundingBox(matrixStack, waypointBox, 1.5f, ColorsModule.getInstance().getRGB(255));
        int dist = (int) Math.sqrt(mc.player.squaredDistanceTo(pos));
        String waypointTag = "§7" + tag + (coordsConfig.getValue() ? String.format(" XYZ %s %s %s", format.format(pos.getX()), format.format(pos.getY()), format.format(pos.getZ())) : "")
                + (distanceConfig.getValue() ? String.format(" %sm", dist) : "");
        RenderManager.renderSign(waypointTag, waypointBox.minX + center, waypointBox.maxY + 0.4, waypointBox.minZ + center, -1);
    }
}
