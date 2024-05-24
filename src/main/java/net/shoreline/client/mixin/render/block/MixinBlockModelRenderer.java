package net.shoreline.client.mixin.render.block;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.shoreline.eventbus.bus.EventBus;
import net.shoreline.client.impl.event.render.block.RenderBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {
    /**
     * @param world
     * @param model
     * @param state
     * @param pos
     * @param matrices
     * @param vertexConsumer
     * @param cull
     * @param random
     * @param seed
     * @param overlay
     * @param ci
     */
    @Inject(method = "render(Lnet/minecraft/world/BlockRenderView;Lnet/" +
            "minecraft/client/render/model/BakedModel;Lnet/minecraft/block/" +
            "BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/" +
            "client/util/math/MatrixStack;Lnet/minecraft/client/render/" +
            "VertexConsumer;ZLnet/minecraft/util/math/random/Random;JI)V",
            at = @At(value = "HEAD"), cancellable = true)
    private void hookRender(BlockRenderView world, BakedModel model,
                            BlockState state, BlockPos pos,
                            MatrixStack matrices,
                            VertexConsumer vertexConsumer, boolean cull,
                            Random random, long seed, int overlay,
                            CallbackInfo ci) {
        RenderBlockEvent renderBlockEvent =
                new RenderBlockEvent(state, pos);
        EventBus.INSTANCE.dispatch(renderBlockEvent);
        if (renderBlockEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
