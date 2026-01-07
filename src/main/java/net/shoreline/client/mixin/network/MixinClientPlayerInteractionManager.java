package net.shoreline.client.mixin.network;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.impl.event.item.ItemUseEvent;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.network.InteractItemEvent;
import net.shoreline.client.impl.event.network.InteractSneakEvent;
import net.shoreline.client.impl.imixin.IClientPlayerInteractionManager;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager implements IClientPlayerInteractionManager
{
    @Accessor("currentBreakingPos")
    public abstract BlockPos getCurrentBreakingPos();

    @Override
    @Invoker(value = "interactBlockInternal", remap = false)
    public abstract ActionResult invokeInteractInternal(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult);

    @Inject(method = "interactItem", at = @At(value = "HEAD"))
    private void hookInteractItemHead(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir)
    {
        ItemStack itemStack = player.getStackInHand(hand);
        InteractItemEvent.Pre interactItemEvent = new InteractItemEvent.Pre(itemStack);
        EventBus.INSTANCE.dispatch(interactItemEvent);
    }

    @Inject(method = "interactItem", at = @At(value = "TAIL"))
    private void hookInteractItemTail(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir)
    {
        ItemStack itemStack = player.getStackInHand(hand);
        InteractItemEvent.Post interactItemEvent = new InteractItemEvent.Post(itemStack);
        EventBus.INSTANCE.dispatch(interactItemEvent);
    }

    @Redirect(method = "interactBlockInternal", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack hookStackInHand(ClientPlayerEntity entity, Hand hand)
    {
        if (hand.equals(Hand.OFF_HAND))
        {
            return entity.getStackInHand(hand);
        }

        final ItemUseEvent.Block event = new ItemUseEvent.Block();
        EventBus.INSTANCE.dispatch(event);
        return event.isCanceled() ? event.getItemStack() : entity.getStackInHand(Hand.MAIN_HAND);
    }

    @Redirect(method = "interactBlockInternal", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldCancelInteraction()Z"))
    private boolean hookInteractBlockInternal(ClientPlayerEntity player)
    {
        InteractSneakEvent packetSneakingEvent = new InteractSneakEvent();
        EventBus.INSTANCE.dispatch(packetSneakingEvent);
        return player.isSneaking() || packetSneakingEvent.isCanceled();
    }

    @Inject(method = "attackBlock", at = @At(value = "HEAD"), cancellable = true)
    private void hookAttackBlock(BlockPos pos,
                                 Direction direction,
                                 CallbackInfoReturnable<Boolean> cir)
    {
        AttackBlockEvent attackBlockEvent = new AttackBlockEvent(pos, direction);
        EventBus.INSTANCE.dispatch(attackBlockEvent);
        if (attackBlockEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(false);
        }
    }
}
