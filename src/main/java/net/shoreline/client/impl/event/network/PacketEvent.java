package net.shoreline.client.impl.event.network;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @since 1.0
 */
public class PacketEvent extends Event
{
    //
    private final Packet<?> packet;

    /**
     * @param packet
     */
    public PacketEvent(Packet<?> packet)
    {
        this.packet = packet;
    }

    /**
     * @return
     */
    public Packet<?> getPacket()
    {
        return packet;
    }

    /**
     *
     */
    @Cancelable
    public static class Inbound extends PacketEvent
    {

        private final PacketListener packetListener;

        /**
         * @param packet
         */
        public Inbound(PacketListener packetListener, Packet<?> packet)
        {
            super(packet);
            this.packetListener = packetListener;
        }

        public PacketListener getPacketListener()
        {
            return packetListener;
        }
    }

    /**
     *
     */
    @Cancelable
    public static class Outbound extends PacketEvent
    {
        //
        private final boolean cached;

        /**
         * @param packet
         */
        public Outbound(Packet<?> packet)
        {
            super(packet);
            this.cached = Managers.NETWORK.isCached(packet);
        }

        /**
         * @return
         */
        public boolean isClientPacket()
        {
            return cached;
        }
    }

    @Cancelable
    public static class OutboundPost extends Outbound
    {
        /**
         * @param packet
         */
        public OutboundPost(Packet<?> packet)
        {
            super(packet);
        }
    }
}
