package net.shoreline.client.mixin.accessor;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(CustomPayloadC2SPacket.class)
public interface AccessorCustomPayloadC2SPacket
{
    /**
     * @param data
     */
    @Accessor("payload")
    @Mutable
    void hookSetData(CustomPayload data);
}
