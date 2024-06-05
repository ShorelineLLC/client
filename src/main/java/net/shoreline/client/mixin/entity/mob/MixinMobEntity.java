package net.shoreline.client.mixin.entity.mob;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PigEntity;
import net.shoreline.client.impl.event.entity.mob.PigAIEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MixinMobEntity
{
    @Inject(method = "isAiDisabled", at = @At(value = "HEAD"), cancellable = true)
    private void hookIsAiDisabled(CallbackInfoReturnable<Boolean> cir)
    {
        MobEntity mobEntity = (MobEntity) (Object) this;
        if (mobEntity instanceof PigEntity)
        {
            PigAIEvent pigAIEvent = new PigAIEvent((PigEntity) mobEntity);
            EventBus.INSTANCE.dispatch(pigAIEvent);
            if (pigAIEvent.isCanceled())
            {
                cir.cancel();
                cir.setReturnValue(true);
            }
        }
    }
}
