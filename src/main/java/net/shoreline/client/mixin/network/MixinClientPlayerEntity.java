package net.shoreline.client.mixin.network;

import net.minecraft.client.network.ClientPlayerEntity;
import net.shoreline.client.impl.event.network.StopSprintingEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity
{
    @Inject(method = "shouldStopSprinting", at = @At(value = "HEAD"), cancellable = true)
    private void hookShouldStopSprinting(CallbackInfoReturnable<Boolean> cir)
    {
        final StopSprintingEvent event = new StopSprintingEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(false);
        }
    }
}
