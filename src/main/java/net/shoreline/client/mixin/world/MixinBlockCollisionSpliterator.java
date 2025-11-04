package net.shoreline.client.mixin.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.CollisionView;
import net.shoreline.client.impl.event.world.BlockCollisionEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockCollisionSpliterator.class)
public class MixinBlockCollisionSpliterator
{
    @Redirect(method = "computeNext",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/ShapeContext;getCollisionShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/CollisionView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;")
    )
    private VoxelShape hookNext(ShapeContext instance, BlockState state, CollisionView view, BlockPos blockPos)
    {
        VoxelShape voxelShape = instance.getCollisionShape(state, view, blockPos);
        if (view != MinecraftClient.getInstance().world)
        {
            return voxelShape;
        }

        BlockCollisionEvent blockCollisionEvent = new BlockCollisionEvent(voxelShape, state, blockPos);
        EventBus.INSTANCE.dispatch(blockCollisionEvent);
        if (blockCollisionEvent.isCanceled())
        {
            return blockCollisionEvent.getCollisionShape();
        }

        return voxelShape;
    }
}
