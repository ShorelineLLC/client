package net.shoreline.client.impl.event.entity.projectile;

import net.minecraft.world.TeleportTarget;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class PearlTeleportEvent extends Event
{
    private final TeleportTarget teleportTarget;

    public PearlTeleportEvent(TeleportTarget teleportTarget)
    {
        this.teleportTarget = teleportTarget;
    }

    public TeleportTarget getTeleportTarget()
    {
        return teleportTarget;
    }
}
