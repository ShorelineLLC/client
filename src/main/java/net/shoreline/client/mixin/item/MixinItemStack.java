package net.shoreline.client.mixin.item;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.shoreline.client.Shoreline;
import net.shoreline.client.impl.event.item.DurabilityEvent;
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
public abstract class MixinItemStack {
    /**
     * @return
     */
    @Shadow
    public abstract int getDamage();

    /**
     * @return
     */
    @Shadow
    public abstract NbtCompound getOrCreateNbt();

    /**
     * @param item
     * @param count
     * @param ci
     */
    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At(
            value = "RETURN"))
    private void hookInitItem(ItemConvertible item, int count, CallbackInfo ci) {
        if (EventBus.EVENT_HANDLER == null) {
            return;
        }
        DurabilityEvent durabilityEvent = new DurabilityEvent(getDamage());
        EventBus.EVENT_HANDLER.dispatch(durabilityEvent);
        if (durabilityEvent.isCanceled()) {
            getOrCreateNbt().putInt("Damage", durabilityEvent.getDamage());
        }
    }

    /**
     * @param nbt
     * @param ci
     */
    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At(
            value = "RETURN"))
    private void hookInitNbt(NbtCompound nbt, CallbackInfo ci) {
        if (EventBus.EVENT_HANDLER == null) {
            return;
        }
        DurabilityEvent durabilityEvent = new DurabilityEvent(nbt.getInt("Damage"));
        EventBus.EVENT_HANDLER.dispatch(durabilityEvent);
        if (durabilityEvent.isCanceled()) {
            getOrCreateNbt().putInt("Damage", durabilityEvent.getDamage());
        }
    }
}
