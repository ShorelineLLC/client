package net.shoreline.client.impl.manager.player;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.shoreline.client.impl.event.network.PacketSneakingEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY;
import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;

public class MovementManager implements Globals
{

    private boolean packetSneaking;

    public MovementManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    /**
     * @param y
     */
    public void setMotionY(double y)
    {
        mc.player.setVelocity(mc.player.getVelocity().getX(), y, mc.player.getVelocity().getZ());
    }

    /**
     * @param x
     * @param z
     */
    public void setMotionXZ(double x, double z)
    {
        mc.player.setVelocity(x, mc.player.getVelocity().y, z);
    }

    public void setMotionX(double x)
    {
        mc.player.setVelocity(x, mc.player.getVelocity().y, mc.player.getVelocity().z);
    }

    public void setMotionZ(double z)
    {
        mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y, z);
    }


    public void setPacketSneaking(final boolean packetSneaking)
    {
        this.packetSneaking = packetSneaking;
        if (packetSneaking)
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, PRESS_SHIFT_KEY));
        }
        else
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, RELEASE_SHIFT_KEY));
        }
    }

    @EventListener
    public void onPacketSneak(PacketSneakingEvent event)
    {
        event.setCanceled(packetSneaking);
    }
}
