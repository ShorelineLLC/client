package net.shoreline.client.impl.event.entity.player;

import net.minecraft.entity.Entity;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class PushEntityEvent extends Event
{
    private final Entity pushed, pusher;

    public PushEntityEvent(Entity pushed, Entity pusher)
    {
        this.pushed = pushed;
        this.pusher = pusher;
    }

    public Entity getPushed()
    {
        return pushed;
    }

    public Entity getPusher()
    {
        return pusher;
    }
}
