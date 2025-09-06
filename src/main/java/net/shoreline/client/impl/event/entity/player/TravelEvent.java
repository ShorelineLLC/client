package net.shoreline.client.impl.event.entity.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@AllArgsConstructor
@Getter
@Setter
public class TravelEvent extends Event
{
    private Vec3d movementInput;

    @Cancelable
    public static class Pre extends TravelEvent {
        public Pre(Vec3d movementInput) {
            super(movementInput);
        }
    }

    public static class Post extends TravelEvent {
        public Post(Vec3d movementInput) {
            super(movementInput);
        }
    }
}
