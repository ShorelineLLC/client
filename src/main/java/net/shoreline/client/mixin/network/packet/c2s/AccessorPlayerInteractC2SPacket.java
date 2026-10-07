package net.shoreline.client.mixin.network.packet.c2s;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerInteractEntityC2SPacket.class)
public interface AccessorPlayerInteractC2SPacket
{
    @Invoker("<init>")
    static PlayerInteractEntityC2SPacket invokeInit(int entityId,
                                                    boolean sneaking,
                                                    PlayerInteractEntityC2SPacket.InteractTypeHandler type)
    {
        throw new AssertionError();
    }
}
