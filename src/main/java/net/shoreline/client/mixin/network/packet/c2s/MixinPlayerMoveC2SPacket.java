package net.shoreline.client.mixin.network.packet.c2s;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.shoreline.client.impl.imixin.IPlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public abstract class MixinPlayerMoveC2SPacket implements IPlayerMoveC2SPacket
{
    @Override
    @Accessor("onGround")
    @Mutable
    public abstract void setOnGround(boolean onGround);

    @Override
    @Accessor("x")
    @Mutable
    public abstract void setX(double x);

    @Override
    @Accessor("y")
    @Mutable
    public abstract void setY(double y);

    @Override
    @Accessor("z")
    @Mutable
    public abstract void setZ(double z);

    @Override
    @Accessor("yaw")
    @Mutable
    public abstract void setYaw(float yaw);

    @Override
    @Accessor("pitch")
    @Mutable
    public abstract void setPitch(float pitch);
}
