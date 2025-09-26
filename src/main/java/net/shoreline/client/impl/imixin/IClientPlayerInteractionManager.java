package net.shoreline.client.impl.imixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

@IMixin
public interface IClientPlayerInteractionManager
{
    ActionResult invokeInteractInternal(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult);
}
