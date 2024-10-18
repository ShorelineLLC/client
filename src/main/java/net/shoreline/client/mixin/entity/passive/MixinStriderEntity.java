package net.shoreline.client.mixin.entity.passive;

import net.minecraft.entity.passive.StriderEntity;
import net.shoreline.client.impl.event.entity.passive.EntitySteerEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StriderEntity.class)
public class MixinStriderEntity
{
    /**
     * @param cir
     */
    @Inject(method = "isSaddled", at = @At(value = "HEAD"), cancellable = true)
    private void hookIsSaddled(CallbackInfoReturnable<Boolean> cir)
    {
        EntitySteerEvent entitySteerEvent = new EntitySteerEvent();
        EventBus.INSTANCE.dispatch(entitySteerEvent);
        if (entitySteerEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }
}
