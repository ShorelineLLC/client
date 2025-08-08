package net.shoreline.client.impl.imixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

@IMixin
public interface IPlayerInteractEntityC2S
{
    Entity getEntity(ClientWorld world);

    PlayerInteractEntityC2SPacket.InteractType getInteractType();
}
