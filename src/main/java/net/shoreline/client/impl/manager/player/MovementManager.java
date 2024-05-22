package net.shoreline.client.impl.manager.player;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.impl.event.handler.EventBus;
import net.shoreline.client.impl.event.network.PacketSneakingEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.Globals;

import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY;
import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;

public class MovementManager implements Globals {

    private boolean packetSneaking;

    public MovementManager() {
        EventBus.EVENT_HANDLER.subscribe(this);
    }

    /**
     * @param y
     */
    public void setMotionY(double y) {
        Vec3d motion = mc.player.getVelocity();
        mc.player.setVelocity(motion.getX(), y, motion.getZ());
    }

    /**
     * @param x
     * @param z
     */
    public void setMotionXZ(double x, double z) {
        Vec3d motion = mc.player.getVelocity();
        mc.player.setVelocity(x, motion.y, z);
    }

    public void setPacketSneaking(final boolean packetSneaking) {
        this.packetSneaking = packetSneaking;
        if (packetSneaking)
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, PRESS_SHIFT_KEY));
        } else
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, RELEASE_SHIFT_KEY));
        }
    }

    @EventListener
    public void onPacketSneak(PacketSneakingEvent event) {
        event.setCanceled(packetSneaking);
    }
}
