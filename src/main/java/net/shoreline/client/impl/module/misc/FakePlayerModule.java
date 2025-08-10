package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IPlayerInteractEntityC2S;
import net.shoreline.client.util.entity.DamageableFakePlayer;
import net.shoreline.eventbus.annotation.EventListener;

public class FakePlayerModule extends Toggleable
{
    private DamageableFakePlayer fakePlayer;

    private final Timer gappleTimer = new NanoTimer();

    public FakePlayerModule()
    {
        super("FakePlayer", "Spawns a fake player", GuiCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        if (!checkNull())
        {
            fakePlayer = new DamageableFakePlayer(mc.player, "FakePlayer");
            fakePlayer.spawnPlayer();
        }
    }

    @Override
    public void onDisable()
    {
        if (!checkNull() && fakePlayer != null && !fakePlayer.isRemoved())
        {
            fakePlayer.despawnPlayer();
        }
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        disable();
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull() || fakePlayer == null)
        {
            return;
        }

        if (gappleTimer.hasPassed(1600))
        {
            fakePlayer.simulateGappleEat();
            gappleTimer.reset();
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof IPlayerInteractEntityC2S packet
                && packet.getInteractType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK)
        {
            final Entity attacked = packet.getEntity(mc.world);
            if (attacked != fakePlayer)
            {
                return;
            }

            fakePlayer.simulateAttackFrom(mc.world, mc.player);
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull() || fakePlayer == null)
        {
            return;
        }

        if (event.getPacket() instanceof ExplosionS2CPacket packet)
        {
            fakePlayer.simulateExplosionFrom(mc.world, packet.center());
        }
    }
}
