package net.shoreline.client.mixin.entity;

import net.minecraft.entity.LimbAnimator;
import net.shoreline.client.impl.event.entity.LimbAnimatorEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author hockeyl8
 * @since 1.0
 */
@Mixin(LimbAnimator.class)
public final class MixinLimbAnimator
{

    @Inject(method = "getSpeed()F", at = @At("HEAD"), cancellable = true)
    private void hookGetSpeed(CallbackInfoReturnable<Float> cir)
    {
        LimbAnimatorEvent limbAnimatorEvent = new LimbAnimatorEvent();
        EventBus.INSTANCE.dispatch(limbAnimatorEvent);
        if (limbAnimatorEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(limbAnimatorEvent.getSpeed());
        }
    }

    @Inject(method = "getSpeed(F)F", at = @At("HEAD"), cancellable = true)
    private void hookGetSpeed(float tickDelta, CallbackInfoReturnable<Float> cir)
    {
        LimbAnimatorEvent limbAnimatorEvent = new LimbAnimatorEvent();
        EventBus.INSTANCE.dispatch(limbAnimatorEvent);
        if (limbAnimatorEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(limbAnimatorEvent.getSpeed());
        }
    }
}