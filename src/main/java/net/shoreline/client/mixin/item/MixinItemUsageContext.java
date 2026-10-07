package net.shoreline.client.mixin.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.shoreline.client.impl.event.item.ItemUseEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemUsageContext.class)
public class MixinItemUsageContext
{
    @Inject(method = "getStack", at = @At(value = "RETURN"), cancellable = true)
    public void hookGetStack(final CallbackInfoReturnable<ItemStack> info)
    {
        final ItemUseEvent.Block event = new ItemUseEvent.Block();
        EventBus.INSTANCE.dispatch(event);
        if (MinecraftClient.getInstance().player != null && event.isCanceled()
                && info.getReturnValue().equals(MinecraftClient.getInstance().player.getMainHandStack()))
        {
            info.setReturnValue(event.getItemStack());
        }
    }
}
