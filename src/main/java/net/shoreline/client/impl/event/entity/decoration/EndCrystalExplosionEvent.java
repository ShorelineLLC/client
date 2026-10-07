package net.shoreline.client.impl.event.entity.decoration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
public class EndCrystalExplosionEvent extends Event
{
    private final Vec3d pos;
    private final DamageSource damageSource;
}
