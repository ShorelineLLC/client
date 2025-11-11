package net.shoreline.client.mixin.entity.player;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.shoreline.client.impl.event.entity.player.InsertStackEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class MixinPlayerInventory
{
    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void hookInsertStack(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir)
    {
        InsertStackEvent slotEvent = new InsertStackEvent(slot, stack);
        EventBus.INSTANCE.dispatch(slotEvent);
        if (slotEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }
}
