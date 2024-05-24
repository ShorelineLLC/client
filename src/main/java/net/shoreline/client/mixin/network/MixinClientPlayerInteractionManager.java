package net.shoreline.client.mixin.network;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.shoreline.eventbus.bus.EventBus;
import net.shoreline.client.impl.event.network.*;
import net.shoreline.client.util.Globals;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @see ClientPlayerInteractionManager
 * @since 1.0
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager implements Globals {
    //
    @Shadow
    private GameMode gameMode;

    @Shadow
    protected abstract void syncSelectedSlot();

    @Shadow
    protected abstract void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);

    /**
     * @param pos
     * @param direction
     * @param cir
     */
    @Inject(method = "attackBlock", at = @At(value = "HEAD"), cancellable = true)
    private void hookAttackBlock(BlockPos pos, Direction direction,
                                 CallbackInfoReturnable<Boolean> cir) {
        BlockState state = mc.world.getBlockState(pos);
        final AttackBlockEvent attackBlockEvent = new AttackBlockEvent(
                pos, state, direction);
        EventBus.INSTANCE.dispatch(attackBlockEvent);
        if (attackBlockEvent.isCanceled()) {
            cir.cancel();
            // cir.setReturnValue(false);
        }
    }

    /**
     * @param cir
     */
    @Inject(method = "getReachDistance", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookGetReachDistance(CallbackInfoReturnable<Float> cir) {
        final ReachEvent reachEvent = new ReachEvent();
        EventBus.INSTANCE.dispatch(reachEvent);
        if (reachEvent.isCanceled()) {
            cir.cancel();
            float reach = gameMode.isCreative() ? 5.0f : 4.5f;
            cir.setReturnValue(reach + reachEvent.getReach());
        }
    }

    /**
     * @param player
     * @param hand
     * @param hitResult
     * @param cir
     */
    @Inject(method = "interactBlock", at = @At(value = "HEAD"), cancellable = true)
    private void hookInteractBlock(ClientPlayerEntity player, Hand hand,
                                   BlockHitResult hitResult,
                                   CallbackInfoReturnable<ActionResult> cir) {
        InteractBlockEvent interactBlockEvent = new InteractBlockEvent(
                player, hand, hitResult);
        EventBus.INSTANCE.dispatch(interactBlockEvent);
        if (interactBlockEvent.isCanceled()) {
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
        }
    }

    /**
     * @param pos
     * @param cir
     */
    @Inject(method = "breakBlock", at = @At(value = "HEAD"), cancellable = true)
    private void hookBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BreakBlockEvent breakBlockEvent = new BreakBlockEvent(pos);
        EventBus.INSTANCE.dispatch(breakBlockEvent);
        if (breakBlockEvent.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    /**
     * @param player
     * @param hand
     * @param cir
     */
    @Inject(method = "interactItem", at = @At(value = "HEAD"), cancellable = true)
    public void hookInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        StrafeFixEvent strafeFixEvent = new StrafeFixEvent();
        EventBus.INSTANCE.dispatch(strafeFixEvent);
        // Strafe fix cuz goofy 1.19 sends move packet when using items
        if (strafeFixEvent.isCanceled()) {
            cir.cancel();
            if (this.gameMode == GameMode.SPECTATOR) {
                cir.setReturnValue(ActionResult.PASS);
            }
            syncSelectedSlot();
            MutableObject mutableObject = new MutableObject();
            sendSequencedPacket(mc.world, sequence -> {
                PlayerInteractItemC2SPacket playerInteractItemC2SPacket = new PlayerInteractItemC2SPacket(hand, sequence);
                ItemStack itemStack = player.getStackInHand(hand);
                if (player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
                    mutableObject.setValue(ActionResult.PASS);
                    return playerInteractItemC2SPacket;
                }
                TypedActionResult<ItemStack> typedActionResult = itemStack.use(mc.world, player, hand);
                ItemStack itemStack2 = typedActionResult.getValue();
                if (itemStack2 != itemStack) {
                    player.setStackInHand(hand, itemStack2);
                }
                mutableObject.setValue(typedActionResult.getResult());
                return playerInteractItemC2SPacket;
            });
            cir.setReturnValue((ActionResult) ((Object) mutableObject.getValue()));
        }
    }

    @Redirect(method = "interactBlockInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldCancelInteraction()Z"))
    private boolean hookRedirectInteractBlockInternal$shouldCancelInteraction(ClientPlayerEntity player)
    {
        PacketSneakingEvent packetSneakingEvent = new PacketSneakingEvent();
        EventBus.INSTANCE.dispatch(packetSneakingEvent);
        return player.isSneaking() || packetSneakingEvent.isCanceled();
    }

    @Redirect(
        method = "interactBlockInternal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack hookRedirectInteractBlockInternal$getStackInHand(ClientPlayerEntity entity, Hand hand) {
        if (hand.equals(Hand.OFF_HAND))
        {
            return entity.getStackInHand(hand);
        }
        ItemDesyncEvent itemDesyncEvent = new ItemDesyncEvent();
        EventBus.INSTANCE.dispatch(itemDesyncEvent);
        return itemDesyncEvent.isCanceled() ? itemDesyncEvent.getServerItem() : entity.getStackInHand(Hand.MAIN_HAND);
    }

    @Redirect(
        method = "interactBlockInternal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
            ordinal = 0))
    private boolean hookRedirectInteractBlockInternal$getMainHandStack(ItemStack instance) {
        ItemDesyncEvent itemDesyncEvent = new ItemDesyncEvent();
        EventBus.INSTANCE.dispatch(itemDesyncEvent);
        return itemDesyncEvent.isCanceled() ? itemDesyncEvent.getServerItem().isEmpty() : instance.isEmpty();
    }
}
