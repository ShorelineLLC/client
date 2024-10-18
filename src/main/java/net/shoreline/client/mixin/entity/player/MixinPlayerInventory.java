package net.shoreline.client.mixin.entity.player;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.shoreline.client.impl.event.entity.player.SetStackEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class MixinPlayerInventory
{

    @Inject(method = "setStack", at = @At(value = "HEAD"))
    private void hookSetStack(int slot, ItemStack stack, CallbackInfo ci)
    {
        SetStackEvent setStackEvent = new SetStackEvent(slot, stack);
        EventBus.INSTANCE.dispatch(setStackEvent);
    }
}
