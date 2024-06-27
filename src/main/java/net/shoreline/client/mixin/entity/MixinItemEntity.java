package net.shoreline.client.mixin.entity;

import net.minecraft.entity.ItemEntity;
import net.shoreline.client.impl.event.entity.ItemTickEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class MixinItemEntity
{
    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    private void hookTick(CallbackInfo ci)
    {
        ItemTickEvent itemTickEvent = new ItemTickEvent();
        EventBus.INSTANCE.dispatch(itemTickEvent);
        if (itemTickEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}
