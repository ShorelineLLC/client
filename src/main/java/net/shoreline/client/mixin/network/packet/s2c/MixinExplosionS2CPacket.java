package net.shoreline.client.mixin.network.packet.s2c;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.imixin.IExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(ExplosionS2CPacket.class)
public abstract class MixinExplosionS2CPacket implements IExplosionS2CPacket
{
    @Override
    @Mutable
    @Accessor("playerKnockback")
    public abstract void setPlayerKnockback(Optional<Vec3d> knockback);
}
