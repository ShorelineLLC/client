package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.packet.Packet;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

public class MovementPacketsEvent extends Event
{
    @Cancelable
    public static class Update extends MovementPacketsEvent {}

    @Cancelable
    @Getter
    @Setter
    public static class Send extends MovementPacketsEvent
    {
        private Packet<?> packet;

        public Send(Packet<?> packet)
        {
            this.packet = packet;
        }
    }
}
