package net.shoreline.client.mixin.network.packet.c2s;

import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.shoreline.client.impl.imixin.IPlayerInteractItemC2S;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInteractItemC2SPacket.class)
public abstract class MixinPlayerInteractItemC2SPacket implements IPlayerInteractItemC2S
{
    @Accessor("yaw")
    @Mutable
    @Override
    public abstract void setYaw(float yaw);

    @Accessor("pitch")
    @Mutable
    @Override
    public abstract void setPitch(float pitch);
}
