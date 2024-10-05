package net.shoreline.client.mixin.render.entity;

import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.item.ItemStack;
import net.shoreline.client.impl.event.render.entity.RenderItemFrameEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFrameEntityRenderer.class)
public class MixinItemFrameEntityRenderer
{
    @Redirect(method = "render(Lnet/minecraft/entity/decoration/ItemFrameEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private boolean redirectIsEmpty(ItemStack itemStack)
    {
        RenderItemFrameEvent renderItemFrameEvent = new RenderItemFrameEvent();
        EventBus.INSTANCE.dispatch(renderItemFrameEvent);
        if (renderItemFrameEvent.isCanceled())
        {
            return true;
        }
        return itemStack.isEmpty();
    }
}
