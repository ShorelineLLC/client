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
        private final boolean isBundled;

        public Inbound(PacketListener packetListener, Packet<?> packet, boolean isBundled)
        {
            super(packet);
            this.packetListener = packetListener;
            this.isBundled = isBundled;
        }
    }

    @Cancelable
    public static class Outbound extends PacketEvent
    {
        public Outbound(Packet<?> packet) {
            super(packet);
        }
    }

    public static class InboundPost extends PacketEvent
    {
        public InboundPost(Packet<?> packet) {
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
