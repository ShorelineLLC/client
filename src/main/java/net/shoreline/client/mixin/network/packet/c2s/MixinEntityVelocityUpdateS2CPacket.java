package net.shoreline.client.mixin.network.packet.c2s;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.shoreline.client.impl.imixin.IEntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public abstract class MixinEntityVelocityUpdateS2CPacket implements IEntityVelocityUpdateS2CPacket
{
    @Override
    @Accessor("velocityX")
    @Mutable
    public abstract void setX(int velocityX);

    @Override
    @Accessor("velocityY")
    @Mutable
    public abstract void setY(int velocityY);

    @Override
    @Accessor("velocityZ")
    @Mutable
    public abstract void setZ(int velocityZ);
}
