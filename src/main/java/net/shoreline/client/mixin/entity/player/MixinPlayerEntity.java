package net.shoreline.client.mixin.entity.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.entity.player.ReachEvent;
import net.shoreline.client.impl.event.entity.player.SprintResetEvent;
import net.shoreline.client.impl.event.entity.player.TravelEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity
{
    @Inject(method = "travel", at = @At(value = "HEAD"), cancellable = true)
    private void hookTravelPre(Vec3d movementInput, CallbackInfo ci)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            TravelEvent.Pre travelEvent = new TravelEvent.Pre(movementInput);
            EventBus.INSTANCE.dispatch(travelEvent);
            if (travelEvent.isCanceled())
            {
                ci.cancel();
            }
        }
    }

    @Inject(method = "travel", at = @At(value = "TAIL"))
    private void hookTravelPost(Vec3d movementInput, CallbackInfo ci)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            TravelEvent.Post travelEvent = new TravelEvent.Post(movementInput);
            EventBus.INSTANCE.dispatch(travelEvent);
        }
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V"))
    private void hookAttack$1(PlayerEntity instance, boolean b)
    {
        if (instance instanceof ClientPlayerEntity)
        {
            SprintResetEvent sprintResetEvent = new SprintResetEvent();
            EventBus.INSTANCE.dispatch(sprintResetEvent);
            if (!sprintResetEvent.isCanceled())
            {
                instance.setSprinting(false);
            }
        }
    }

    @Inject(method = "getBlockInteractionRange", at = @At(value = "RETURN"), cancellable = true)
    private void hookGetBlockInteractionRange(CallbackInfoReturnable<Double> cir)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            final ReachEvent reachEvent = new ReachEvent();
            EventBus.INSTANCE.dispatch(reachEvent);
            if (reachEvent.isCanceled())
            {
                cir.cancel();
                cir.setReturnValue(cir.getReturnValueD() + reachEvent.getReach());
            }
        }

    }

    @Inject(method = "getEntityInteractionRange", at = @At(value = "RETURN"), cancellable = true)
    private void hookGetEntityInteractionRange(CallbackInfoReturnable<Double> cir)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            final ReachEvent reachEvent = new ReachEvent();
            EventBus.INSTANCE.dispatch(reachEvent);
            if (reachEvent.isCanceled())
            {
                cir.cancel();
                cir.setReturnValue(cir.getReturnValueD() + reachEvent.getReach());
            }
        }
    }
}
