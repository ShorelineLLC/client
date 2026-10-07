package net.shoreline.client.mixin.network.packet.c2s;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.shoreline.client.impl.imixin.IPlayerInteractEntityC2S;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerInteractEntityC2SPacket.class)
public abstract class MixinPlayerInteractC2SPacket implements IPlayerInteractEntityC2S
{
    @Shadow
    @Final
    private PlayerInteractEntityC2SPacket.InteractTypeHandler type;

    @Shadow
    @Final
    private int entityId;

    @Override
    public Entity getEntity(ClientWorld world)
    {
        return world.getEntityById(entityId);
    }

    @Override
    public PlayerInteractEntityC2SPacket.InteractType getInteractType()
    {
        return type.getType();
    }
}
