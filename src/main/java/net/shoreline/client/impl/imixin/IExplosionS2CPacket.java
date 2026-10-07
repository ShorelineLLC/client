package net.shoreline.client.impl.imixin;

import net.minecraft.util.math.Vec3d;

import java.util.Optional;

@IMixin
public interface IExplosionS2CPacket
{
    void setPlayerKnockback(Optional<Vec3d> knockback);
}
