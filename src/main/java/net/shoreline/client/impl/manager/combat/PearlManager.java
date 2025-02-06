package net.shoreline.client.impl.manager.combat;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.module.exploit.PhaseModule;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.player.RayCastUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

public class PearlManager implements Globals
{
    private float[] lastThrownAngles;
    private Box pearlBB;

    public PearlManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || !PhaseModule.getInstance().shouldRaytrace())
        {
            return;
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet && lastThrownAngles != null)
        {
            BlockHitResult hitResult = (BlockHitResult) RayCastUtil.rayCast(3.0, lastThrownAngles);
            pearlBB = new Box(hitResult.getPos().subtract(0.4, 0.4, 0.4),
                    hitResult.getPos().add(0.4, 0.4, 0.4));

            if (mc.world.getBlockState(hitResult.getBlockPos()).isAir())
            {
                return;
            }

            if (!pearlBB.contains(packet.getX(), packet.getY(), packet.getZ()))
            {
                event.cancel();
                mc.getNetworkHandler().getConnection().send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
                mc.getNetworkHandler().getConnection().send(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(),
                        mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false));
            }
            lastThrownAngles = null;
        }
    }

    public void setLastThrownAngles(float[] lastThrownAngles)
    {
        this.lastThrownAngles = lastThrownAngles;
    }
}
