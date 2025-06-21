package net.shoreline.client.impl.event.entity;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

public class PlayerJumpEvent extends Event
{
    @Cancelable
    public static class Pre extends PlayerJumpEvent {}

    public static class Post extends PlayerJumpEvent {}

    @Cancelable
    @Getter
    @Setter
    public static class Yaw extends PlayerJumpEvent
    {
        private float yaw;

        public Yaw(float yaw)
        {
            this.yaw = yaw;
        }
    }
}
