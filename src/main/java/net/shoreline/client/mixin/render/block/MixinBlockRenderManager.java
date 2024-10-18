package net.shoreline.client.mixin.render.block;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.shoreline.client.impl.event.render.block.RenderBlockEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(BlockRenderManager.class)
public class MixinBlockRenderManager
{
    // This bs is incompatible with sodium
    @Inject(method = "renderBlock", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderBlock(BlockState state, BlockPos pos, BlockRenderView world,
                                 MatrixStack matrices, VertexConsumer vertexConsumer,
                                 boolean cull, Random random, CallbackInfo ci)
    {
        RenderBlockEvent renderBlockEvent =
                new RenderBlockEvent(state, pos);
        EventBus.INSTANCE.dispatch(renderBlockEvent);
        if (renderBlockEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}
