package net.shoreline.client.mixin.accessor;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author linus
 * @see AccessorExplosionS2CPacket
 * @since 1.0
 */
@Mixin(ExplosionS2CPacket.class)
public interface AccessorExplosionS2CPacket
{
    /**
     * @param playerVelocityX
     */
    @Accessor("playerVelocityX")
    @Mutable
    void setPlayerVelocityX(float playerVelocityX);

    /**
     * @param playerVelocityY
     */
    @Accessor("playerVelocityY")
    @Mutable
    void setPlayerVelocityY(float playerVelocityY);

    /**
     * @param playerVelocityZ
     */
    @Accessor("playerVelocityZ")
    @Mutable
    void setPlayerVelocityZ(float playerVelocityZ);
}
