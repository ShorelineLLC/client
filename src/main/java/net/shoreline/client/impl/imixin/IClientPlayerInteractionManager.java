package net.shoreline.client.impl.imixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

@IMixin
public interface IClientPlayerInteractionManager
{
    BlockPos getCurrentBreakingPos();

    ActionResult invokeInteractInternal(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult);
}
