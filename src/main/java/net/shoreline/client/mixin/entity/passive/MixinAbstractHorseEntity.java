package net.shoreline.client.mixin.entity.passive;

import net.minecraft.entity.passive.AbstractHorseEntity;
import net.shoreline.client.impl.event.network.MountEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public class MixinAbstractHorseEntity
{
    @Inject(method = "isSaddled", at = @At(value = "HEAD"), cancellable = true)
    private void hookIsSaddled(CallbackInfoReturnable<Boolean> cir)
    {
        MountEvent entitySteerEvent = new MountEvent();
        EventBus.INSTANCE.dispatch(entitySteerEvent);
        if (entitySteerEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }
}
