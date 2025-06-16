package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.StageEvent;

@RequiredArgsConstructor
@Getter
public class PacketEvent extends StageEvent
{
    private final Packet<?> packet;

    @Cancelable
    @Getter
    public static class Inbound extends PacketEvent
    {
        private final PacketListener packetListener;

        public Inbound(PacketListener packetListener, Packet<?> packet)
        {
            super(packet);
            this.packetListener = packetListener;
        }
    }

    @Cancelable
    public static class Outbound extends PacketEvent
    {
        public Outbound(Packet<?> packet) {
            super(packet);
        }
    }
}
