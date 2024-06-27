package net.shoreline.client.mixin.accessor;

import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VehicleMoveC2SPacket.class)
public interface AccessorVehicleMoveC2SPacket
{
    @Accessor("y")
    void hookSetY(double y);
}
