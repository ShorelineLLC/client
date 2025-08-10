package net.shoreline.client.impl.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;

@RequiredArgsConstructor
@Getter
@Setter
public class EntityState
{
    private final Vec3d pos;
    private final float totalHealth;
}