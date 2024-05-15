package net.shoreline.client.impl.module.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.api.waypoint.Waypoint;
import net.shoreline.client.impl.event.ScreenOpenEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.init.Modules;

import java.text.DecimalFormat;
import java.util.UUID;

/**
 * @author linus
 * @since 1.0
 */
public class WaypointsModule extends ToggleModule {

    Config<Boolean> logoutsConfig = new BooleanConfig("LogoutPoints", "Marks the position of player logouts", false);
    Config<Boolean> deathsConfig = new BooleanConfig("DeathPoints", "Marks the position of player deaths", false);

    public WaypointsModule() {
        super("Waypoints", "Renders a waypoint at marked locations", ModuleCategory.RENDER);
    }

    @Override
    public void onDisable() {
        Managers.WAYPOINT.clear();
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (mc.world == null) {
            return;
        }
        if (event.getPacket() instanceof PlayerListS2CPacket packet && packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER) && logoutsConfig.getValue()) {
            for (PlayerListS2CPacket.Entry entry : packet.getPlayerAdditionEntries()) {
                GameProfile profile = entry.profile();
                if (profile == null || profile.getName() == null) {
                    continue;
                }
                Managers.WAYPOINT.remove(String.format("%s's Logout", profile.getName()));
            }
        } else if (event.getPacket() instanceof PlayerRemoveS2CPacket packet && logoutsConfig.getValue()) {
            for (UUID id : packet.profileIds()) {
                PlayerEntity player = mc.world.getPlayerByUuid(id);
                if (player == null) {
                    continue;
                }
                String ip = Managers.NETWORK.getServerIp();
                String serverIp = mc.isInSingleplayer() ? "Singleplayer" : ip;
                DecimalFormat format = new DecimalFormat("0.0");
                Managers.WAYPOINT.register(new Waypoint(String.format("%s's Logout XYZ %s %s %s",
                        player.getName().getString(), format.format(player.prevX), format.format(player.prevY), format.format(player.prevZ)),
                        serverIp, player.prevX, player.prevY, player.prevZ));
            }
        }
    }

    @EventListener
    public void onRemoveEntity(ScreenOpenEvent event) {
        if (event.getScreen() instanceof DeathScreen && deathsConfig.getValue()) {
            String serverIp = mc.isInSingleplayer() ? "Singleplayer" : Managers.NETWORK.getServerIp();
            Managers.WAYPOINT.remove("Last Death");
            Managers.WAYPOINT.register(new Waypoint("Last Death", serverIp,
                    mc.player.lastX, mc.player.lastBaseY, mc.player.lastZ));
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.player == null) {
            return;
        }
        for (Waypoint waypoint : Managers.WAYPOINT.getWaypoints()) {
            Box waypointBox = EntityDimensions.fixed(0.6f, 2.2f).getBoxAt(waypoint.getPos());
            double center = (waypointBox.maxX - waypointBox.minX) / 2.0f;
            RenderManager.renderBoundingBox(event.getMatrices(), waypointBox, 2.5f, Modules.COLORS.getRGB(255));
            RenderManager.renderSign(event.getMatrices(), waypoint.getName(),
                    new Vec3d(waypointBox.minX + center, waypointBox.maxY + 0.4, waypointBox.minZ + center));
        }
    }
}
