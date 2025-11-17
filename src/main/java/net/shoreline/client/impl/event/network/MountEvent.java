package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PigEntity;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
public class MountEvent extends Event
{
    @Cancelable
    @Getter
    @Setter
    public static class JumpStrength extends MountEvent
    {
        private float jumpStrength;
    }

    @Cancelable
    @RequiredArgsConstructor
    public static class PigAI extends MountEvent
    {
        private final PigEntity pigEntity;

        public boolean isMounted(Entity entity)
        {
            return pigEntity.getPassengerList().contains(entity);
        }
    }
}
