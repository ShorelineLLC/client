package net.shoreline.client.mixin.accessor;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface AccessorPlayerMoveC2SPacket
{
    @Accessor("onGround")
    @Mutable
    void setOnGround(boolean onGround);

    @Accessor("x")
    @Mutable
    void setX(double x);

    @Accessor("y")
    @Mutable
    void setY(double y);

    @Accessor("z")
    @Mutable
    void setZ(double z);

    @Accessor("yaw")
    @Mutable
    void setYaw(float yaw);

    @Accessor("pitch")
    @Mutable
    void setPitch(float pitch);
}
