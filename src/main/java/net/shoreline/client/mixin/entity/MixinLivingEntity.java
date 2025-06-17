package net.shoreline.client.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.shoreline.client.impl.event.entity.JumpYawEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class MixinLivingEntity
{
    @ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
    private float hookJump$getYaw(float original)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            JumpYawEvent event = new JumpYawEvent(original);
            EventBus.INSTANCE.dispatch(event);
            if (event.isCanceled())
            {
                return event.getYaw();
            }
        }

        return original;
    }
}
