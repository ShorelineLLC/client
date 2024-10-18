package net.shoreline.client.mixin.render.block;

import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.impl.event.render.block.RenderTileEntityEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(EnchantingTableBlockEntityRenderer.class)
public class MixinEnchantingTableBlockEntityRenderer
{
    @Shadow
    @Final
    private BookModel book;

    /**
     * DO NOT CHANGE TO REDIRECT
     */
    @Inject(method = "render(Lnet/minecraft/block/entity/EnchantingTable" +
            "BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet" +
            "/minecraft/client/render/VertexConsumerProvider;II)V", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BookModel;renderBook(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
            shift = At.Shift.BEFORE), cancellable = true)
    private void hookRender(EnchantingTableBlockEntity enchantingTableBlockEntity,
                            float f, MatrixStack matrixStack,
                            VertexConsumerProvider vertexConsumerProvider,
                            int i, int j, CallbackInfo ci)
    {
        RenderTileEntityEvent.EnchantingTableBook renderTileEntityEvent =
                new RenderTileEntityEvent.EnchantingTableBook();
        EventBus.INSTANCE.dispatch(renderTileEntityEvent);
        if (renderTileEntityEvent.isCanceled())
        {
            ci.cancel();
            matrixStack.pop();
        }
    }
}
