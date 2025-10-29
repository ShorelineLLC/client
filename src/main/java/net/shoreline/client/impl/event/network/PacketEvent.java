package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Getter
public class PacketEvent extends Event
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

    public static class OutboundPost extends PacketEvent
    {
        public OutboundPost(Packet<?> packet) {
            super(packet);
        }
    }
}
