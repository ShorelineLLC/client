package net.shoreline.client.mixin.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.network.InputMovementEvent;
import net.shoreline.client.impl.event.network.PlayerMoveEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.event.network.StopSprintingEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity
{
    @Shadow
    public Input input;

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V"),
            cancellable = true)
    private void hookTickPre(CallbackInfo ci)
    {
        if ((Object) this != MinecraftClient.getInstance().player)
        {
            return;
        }

        final PlayerUpdateEvent.Pre event = new PlayerUpdateEvent.Pre();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "HEAD"))
    private void hookSendMovementPackets(CallbackInfo ci)
    {
        if ((Object) this != MinecraftClient.getInstance().player)
        {
            return;
        }

        final PlayerUpdateEvent.PrePacket event = new PlayerUpdateEvent.PrePacket();
        EventBus.INSTANCE.dispatch(event);
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "TAIL"))
    private void hookSendMovementPacketsPost(CallbackInfo ci)
    {
        if ((Object) this != MinecraftClient.getInstance().player)
        {
            return;
        }

        final PlayerUpdateEvent.Post event = new PlayerUpdateEvent.Post();
        EventBus.INSTANCE.dispatch(event);
    }

    @ModifyArgs(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"))
    private void hookMove(Args args)
    {
        final MovementType par1 = args.get(0);
        final Vec3d par2 = args.get(1);
        PlayerMoveEvent event = new PlayerMoveEvent(par1, par2);
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            args.set(1, event.getMovement());
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/input/Input;tick()V",
            shift = At.Shift.AFTER))
    private void hookTickMovement(CallbackInfo ci)
    {
        InputMovementEvent event = new InputMovementEvent(input);
        EventBus.INSTANCE.dispatch(event);
    }

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
