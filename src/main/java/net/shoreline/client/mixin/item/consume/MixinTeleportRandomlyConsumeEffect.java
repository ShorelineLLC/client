package net.shoreline.client.mixin.item.consume;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.TeleportRandomlyConsumeEffect;
import net.minecraft.world.World;
import net.shoreline.client.impl.event.item.consume.TeleportConsumeEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TeleportRandomlyConsumeEffect.class)
public class MixinTeleportRandomlyConsumeEffect
{
    @Inject(method = "onConsume", at = @At(value = "HEAD"), cancellable = true)
    private void hookOnConsume(World world,
                               ItemStack stack,
                               LivingEntity user,
                               CallbackInfoReturnable<Boolean> cir)
    {
        TeleportConsumeEvent teleportConsumeEvent = new TeleportConsumeEvent(stack);
        EventBus.INSTANCE.dispatch(teleportConsumeEvent);
        if (teleportConsumeEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }
}
