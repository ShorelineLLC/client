package net.shoreline.client.impl.ac;

import lombok.Getter;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

public class AnticheatManager extends GenericFeature
{
    @Getter
    private SetbackData lastSetback;

    public AnticheatManager()
    {
        super("Anticheat");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onPacketInbound(final PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet)
        {
            PlayerPosition position = packet.change();
            lastSetback = new SetbackData(position.position(),
                    System.currentTimeMillis(), packet.teleportId());
        }
    }

    public boolean hasPassedSinceSetback(final long timeMS)
    {
        return lastSetback != null && lastSetback.timeSince() >= timeMS;
    }
}
