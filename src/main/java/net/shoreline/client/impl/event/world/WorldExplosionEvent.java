package net.shoreline.client.impl.event.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.explosion.ExplosionImpl;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
public class WorldExplosionEvent extends Event
{
    private final ExplosionImpl explosion;
}
