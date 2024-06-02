package net.shoreline.client.impl.module.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.math.Box;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.api.waypoint.Waypoint;
import net.shoreline.client.impl.event.ScreenOpenEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;

import java.text.DecimalFormat;
import java.util.UUID;

/**
 * @author linus
 * @since 1.0
 */
public class WaypointsModule extends ToggleModule {
    private static WaypointsModule INSTANCE;

    Config<Boolean> logoutsConfig = register(new BooleanConfig("LogoutPoints", "Marks the position of player logouts", false));
    Config<Boolean> deathsConfig = register(new BooleanConfig("DeathPoints", "Marks the position of player deaths", false));
    Config<Boolean> coordsConfig = register(new BooleanConfig("Coords", "Shows the coordinates of the waypoint", true));
    DecimalFormat format = new DecimalFormat("0.0");

    public WaypointsModule() {
        super("Waypoints", "Renders a waypoint at marked locations", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    public static WaypointsModule getInstance() {
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        Managers.WAYPOINT.clear();
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof PlayerListS2CPacket packet && packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER) && logoutsConfig.getValue()) {
            for (PlayerListS2CPacket.Entry entry : packet.getPlayerAdditionEntries()) {
                GameProfile profile = entry.profile();
                if (profile == null || profile.getName() == null) {
                    continue;
                }
                Managers.WAYPOINT.removeContains(profile.getName());
            }
        } else if (event.getPacket() instanceof PlayerRemoveS2CPacket packet && logoutsConfig.getValue()) {
            for (UUID id : packet.profileIds()) {
                PlayerEntity player = mc.world.getPlayerByUuid(id);
                if (player == null) {
                    continue;
                }
                String serverIp = mc.isInSingleplayer() ? "Singleplayer" : Managers.NETWORK.getServerIp();
                String nametag = String.format("%s Logout" + (coordsConfig.getValue() ? " XYZ %s %s %s" : ""), player.getName().getString(), format.format(mc.player.getX()), format.format(mc.player.getY()), format.format(mc.player.getZ()));
                nametag = (Managers.SOCIAL.isFriend(player.getName().getString()) ? "§b" : "§7") + nametag;
                Managers.WAYPOINT.register(new Waypoint(nametag, serverIp, player.getX(), player.getY(), player.getZ()));
            }
        }
    }

    @EventListener
    public void onRemoveEntity(ScreenOpenEvent event) {
        if (event.getScreen() instanceof DeathScreen && deathsConfig.getValue()) {
            String serverIp = mc.isInSingleplayer() ? "Singleplayer" : Managers.NETWORK.getServerIp();
            Managers.WAYPOINT.removeContains("Last Death");
            Managers.WAYPOINT.register(new Waypoint(String.format("§7Last Death" + (coordsConfig.getValue() ? " XYZ %s %s %s" : ""), format.format(mc.player.getX()), format.format(mc.player.getY()), format.format(mc.player.getZ())), serverIp,
                    mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.player == null) {
            return;
        }
        for (Waypoint waypoint : Managers.WAYPOINT.getWaypoints()) {
            if (!waypoint.getIp().equalsIgnoreCase(Managers.NETWORK.getServerIp())) {
                continue;
            }
            Box waypointBox = EntityDimensions.fixed(0.6f, 2.2f).getBoxAt(waypoint.getPos());
            double center = (waypointBox.maxX - waypointBox.minX) / 2.0f;
            RenderManager.renderBoundingBox(event.getMatrices(), waypointBox, 1.5f, ColorsModule.getInstance().getRGB(255));
            RenderManager.renderSign(waypoint.getName(), waypointBox.minX + center, waypointBox.maxY + 0.4, waypointBox.minZ + center, -1);
        }
    }

    public boolean getCoords() {
        return coordsConfig.getValue();
    }
}
