package net.shoreline.client.mixin.item;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.shoreline.client.impl.event.item.DurabilityEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ItemStack.class)
public abstract class MixinItemStack
{
    /**
     * @return
     */
    @Shadow
    public abstract int getDamage();

    @Shadow
    public abstract void setDamage(int damage);

    /**
     * @param item
     * @param count
     * @param ci
     */
    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At(value = "RETURN"))
    private void hookInitItem(ItemConvertible item, int count, CallbackInfo ci)
    {
        if (EventBus.INSTANCE == null)
        {
            return;
        }
        DurabilityEvent durabilityEvent = new DurabilityEvent(getDamage());
        EventBus.INSTANCE.dispatch(durabilityEvent);
        if (durabilityEvent.isCanceled())
        {
            setDamage(durabilityEvent.getDamage());
        }
    }
}
