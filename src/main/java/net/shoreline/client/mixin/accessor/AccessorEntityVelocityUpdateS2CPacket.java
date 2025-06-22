package net.shoreline.client.mixin.accessor;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface AccessorEntityVelocityUpdateS2CPacket
{
    @Accessor("velocityX")
    void setX(int velocityX);

    @Accessor("velocityY")
    void setY(int velocityY);

    @Accessor("velocityZ")
    void setZ(int velocityZ);
}
