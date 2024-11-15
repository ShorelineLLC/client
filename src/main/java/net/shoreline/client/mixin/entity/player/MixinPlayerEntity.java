package net.shoreline.client.mixin.entity.player;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shoreline.client.impl.event.entity.player.*;
import net.shoreline.client.impl.event.network.ReachEvent;
import net.shoreline.client.impl.event.render.entity.ElytraTransformEvent;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.event.StageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements Globals
{
    /**
     * @param entityType
     * @param world
     */
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world)
    {
        super(entityType, world);
    }

    @Shadow
    public abstract void travel(Vec3d movementInput);

    /**
     * @param movementInput
     * @param ci
     */
    @Inject(method = "travel", at = @At(value = "HEAD"), cancellable = true)
    private void hookTravelHead(Vec3d movementInput, CallbackInfo ci)
    {
        TravelEvent travelEvent = new TravelEvent(movementInput);
        EventBus.INSTANCE.dispatch(travelEvent);
        if (travelEvent.isCanceled())
        {
            move(MovementType.SELF, getVelocity());
            ci.cancel();
        }
    }

    /**
     * @param cir
     */
    @Inject(method = "isPushedByFluids", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookIsPushedByFluids(CallbackInfoReturnable<Boolean> cir)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        PushFluidsEvent pushFluidsEvent = new PushFluidsEvent();
        EventBus.INSTANCE.dispatch(pushFluidsEvent);
        if (pushFluidsEvent.isCanceled())
        {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    /**
     * @param ci
     */
    @Inject(method = "jump", at = @At(value = "HEAD"), cancellable = true)
    private void hookJumpPre(CallbackInfo ci)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        PlayerJumpEvent playerJumpEvent = new PlayerJumpEvent();
        playerJumpEvent.setStage(StageEvent.EventStage.PRE);
        EventBus.INSTANCE.dispatch(playerJumpEvent);
        if (playerJumpEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    /**
     * @param ci
     */
    @Inject(method = "jump", at = @At(value = "RETURN"), cancellable = true)
    private void hookJumpPost(CallbackInfo ci)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        PlayerJumpEvent playerJumpEvent = new PlayerJumpEvent();
        playerJumpEvent.setStage(StageEvent.EventStage.POST);
        EventBus.INSTANCE.dispatch(playerJumpEvent);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void hookAttack(PlayerEntity playerEntity, Vec3d movementInput)
    {
        if (playerEntity instanceof ClientPlayerEntity)
        {
            SprintResetEvent sprintResetEvent = new SprintResetEvent();
            EventBus.INSTANCE.dispatch(sprintResetEvent);
            if (!sprintResetEvent.isCanceled())
            {
                mc.player.setVelocity(mc.player.getVelocity().multiply(0.6, 1.0, 0.6));
            }
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
                mc.player.setSprinting(false);
            }
        }
    }

    @Inject(method = "clipAtLedge", at = @At(value = "HEAD"), cancellable = true)
    private void hookClipAtLedge(CallbackInfoReturnable<Boolean> cir)
    {
        LedgeClipEvent ledgeClipEvent = new LedgeClipEvent();
        EventBus.INSTANCE.dispatch(ledgeClipEvent);
        if (ledgeClipEvent.isCanceled())
        {
            cir.setReturnValue(ledgeClipEvent.isClipped());
        }
    }

    @Redirect(method = "updatePose", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isFallFlying()Z"))
    private boolean hookUpdatePose(PlayerEntity instance)
    {
        ElytraTransformEvent elytraTransformEvent = new ElytraTransformEvent(instance);
        EventBus.INSTANCE.dispatch(elytraTransformEvent);
        if (elytraTransformEvent.isCanceled())
        {
            return false;
        }
        return instance.isFallFlying();
    }

    @Inject(method = "getBlockInteractionRange", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetBlockInteractionRange(CallbackInfoReturnable<Double> cir)
    {
        final ReachEvent.Block reachEvent = new ReachEvent.Block();
        EventBus.INSTANCE.dispatch(reachEvent);
        if (reachEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(reachEvent.getReach());
        }
    }

    @Inject(method = "getEntityInteractionRange", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetEntityInteractionRange(CallbackInfoReturnable<Double> cir)
    {
        final ReachEvent.Entity reachEvent = new ReachEvent.Entity();
        EventBus.INSTANCE.dispatch(reachEvent);
        if (reachEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(reachEvent.getReach());
        }
    }
}
