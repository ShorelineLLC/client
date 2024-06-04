package net.shoreline.client.mixin.accessor;

import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerPositionLookS2CPacket.class)
public interface AccessorPlayerPositionLookS2CPacket
{
    /**
     * @param yaw
     */
    @Accessor("yaw")
    @Mutable
    void setYaw(float yaw);

    /**
     * @param pitch
     */
    @Accessor("pitch")
    @Mutable
    void setPitch(float pitch);
}
