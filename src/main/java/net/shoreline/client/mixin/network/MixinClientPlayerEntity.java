package net.shoreline.client.mixin.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.network.*;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity
{
    @Shadow
    public abstract void tick();

    @Shadow
    private int ticksSinceLastPositionPacketSent;

    @Unique
    private boolean ticking;

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V",
            shift = At.Shift.BEFORE))
    private void hookTickPre(CallbackInfo ci)
    {
        final PlayerUpdateEvent.Pre event = new PlayerUpdateEvent.Pre();
        EventBus.INSTANCE.dispatch(event);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V",
            shift = At.Shift.AFTER))
    private void hookTickPeri(CallbackInfo ci)
    {
        final PlayerUpdateEvent.Peri event = new PlayerUpdateEvent.Peri();
        EventBus.INSTANCE.dispatch(event);
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "HEAD"))
    private void hookSendMovementPackets(CallbackInfo ci)
    {
        final PlayerUpdateEvent.PrePacket event = new PlayerUpdateEvent.PrePacket();
        EventBus.INSTANCE.dispatch(event);

        MovementPacketsEvent.Update packetsEvent = new MovementPacketsEvent.Update();
        EventBus.INSTANCE.dispatch(packetsEvent);
        if (packetsEvent.isCanceled())
        {
            ticksSinceLastPositionPacketSent = 20;
        }
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "TAIL"))
    private void hookSendMovementPacketsPost(CallbackInfo ci)
    {
        final PlayerUpdateEvent.Post event = new PlayerUpdateEvent.Post();
        EventBus.INSTANCE.dispatch(event);
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void hookSendMovementPacket(ClientPlayNetworkHandler instance, Packet<?> packet)
    {
        MovementPacketsEvent.Send packetsEvent = new MovementPacketsEvent.Send(packet);
        EventBus.INSTANCE.dispatch(packetsEvent);
        if (packetsEvent.isCanceled())
        {
            instance.sendPacket(packetsEvent.getPacket());
            return;
        }

        instance.sendPacket(packet);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V",
            ordinal = 0,
            shift = At.Shift.AFTER))
    private void hookTick(CallbackInfo ci)
    {
        if (ticking)
        {
            return;
        }

        TickMovementEvent tickMovementEvent = new TickMovementEvent();
        EventBus.INSTANCE.dispatch(tickMovementEvent);
        if (tickMovementEvent.isCanceled())
        {
            for (int i = 0; i < tickMovementEvent.getIterations(); i++)
            {
                ticking = true;
                tick();
                ticking = false;
            }
        }
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

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean hookIsUsingItem(ClientPlayerEntity instance)
    {
        MovementFactorEvent.Item event = new MovementFactorEvent.Item();
        EventBus.INSTANCE.dispatch(event);
        return !event.isCanceled() && instance.isUsingItem();
    }

    @Redirect(method = "shouldStopSprinting", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean hookIsUsingItem$2(ClientPlayerEntity instance)
    {
        MovementFactorEvent.Item event = new MovementFactorEvent.Item();
        EventBus.INSTANCE.dispatch(event);
        return !event.isCanceled() && instance.isUsingItem();
    }

    // Fuck you fabric...
    @Inject(method = "shouldSlowDown", at = @At(value = "HEAD"), cancellable = true)
    private void hookShouldSlowdown(CallbackInfoReturnable<Boolean> cir)
    {
        MovementFactorEvent.Slowdown event = new MovementFactorEvent.Slowdown();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(false);
        }
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

    @Inject(method = "pushOutOfBlocks", at = @At(value = "HEAD"), cancellable = true)
    private void hookPushOutOfBlocks(double x, double z, CallbackInfo ci)
    {
        PushOutOfBlocksEvent event = new PushOutOfBlocksEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "swingHand", at = @At(value = "RETURN"))
    private void hookSwingHand(Hand hand, CallbackInfo ci)
    {
        SwingHandEvent swingEvent = new SwingHandEvent(hand);
        EventBus.INSTANCE.dispatch(swingEvent);
    }

    @Inject(method = "setCurrentHand", at = @At(value = "HEAD"))
    private void hookSetCurrentHand(Hand hand, CallbackInfo ci)
    {
        SetHandEvent setHandEvent = new SetHandEvent();
        EventBus.INSTANCE.dispatch(setHandEvent);
    }

    @Inject(method = "getMountJumpStrength", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetMountJumpStrength(CallbackInfoReturnable<Float> cir)
    {
        MountEvent.JumpStrength mountJumpStrengthEvent = new MountEvent.JumpStrength();
        EventBus.INSTANCE.dispatch(mountJumpStrengthEvent);
        if (mountJumpStrengthEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(mountJumpStrengthEvent.getJumpStrength());
        }
    }

    /** Allows you to open screens in portals **/
    @Redirect(method = "tickNausea", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"))
    private Screen hookCurrentScreen(MinecraftClient instance)
    {
        return null;
    }
}
