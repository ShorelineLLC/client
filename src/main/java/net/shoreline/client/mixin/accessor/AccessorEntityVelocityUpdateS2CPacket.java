package net.shoreline.client.mixin.accessor;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Gavin
 * @since 1.0
 */
@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface AccessorEntityVelocityUpdateS2CPacket
{
    @Accessor("velocityX")
    @Mutable
    void setVelocityX(int velocityX);

    @Accessor("velocityY")
    @Mutable
    void setVelocityY(int velocityY);

    @Accessor("velocityZ")
    @Mutable
    void setVelocityZ(int velocityZ);
}
