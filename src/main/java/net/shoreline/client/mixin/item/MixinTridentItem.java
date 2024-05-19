package net.shoreline.client.mixin.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shoreline.client.impl.event.handler.EventBus;
import net.shoreline.client.impl.event.item.TridentPullbackEvent;
import net.shoreline.client.impl.event.item.TridentWaterEvent;
import net.shoreline.client.util.Globals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TridentItem.class)
public abstract class MixinTridentItem implements Globals {

    @Shadow
    public abstract int getMaxUseTime(ItemStack stack);

    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    private void hookUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        TridentWaterEvent tridentWaterEvent = new TridentWaterEvent();
        EventBus.EVENT_HANDLER.dispatch(tridentWaterEvent);
        if (tridentWaterEvent.isCanceled()) {
            cir.cancel();
            ItemStack itemStack = user.getStackInHand(hand);
            if (itemStack.getDamage() >= itemStack.getMaxDamage() - 1) {
                cir.setReturnValue(TypedActionResult.fail(itemStack));
                return;
            }
            user.setCurrentHand(hand);
            cir.setReturnValue(TypedActionResult.consume(itemStack));
        }
    }

    @Inject(method = "onStoppedUsing", at = @At(value = "HEAD"), cancellable = true)
    private void hookOnStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (!(user instanceof PlayerEntity)) {
            return;
        }
        int i = getMaxUseTime(stack) - remainingUseTicks;
        TridentPullbackEvent tridentPullbackEvent = new TridentPullbackEvent();
        EventBus.EVENT_HANDLER.dispatch(tridentPullbackEvent);
        if (!tridentPullbackEvent.isCanceled() && i < 10) {
            return;
        }
        TridentWaterEvent tridentWaterEvent = new TridentWaterEvent();
        EventBus.EVENT_HANDLER.dispatch(tridentWaterEvent);
        if (tridentWaterEvent.isCanceled()) {
            ci.cancel();
            PlayerEntity playerEntity = (PlayerEntity) user;
            int j = EnchantmentHelper.getRiptide(stack);
            if (!mc.world.isClient) {
                stack.damage(1, playerEntity, p -> p.sendToolBreakStatus(user.getActiveHand()));
                if (j == 0) {
                    TridentEntity tridentEntity = new TridentEntity(world, playerEntity, stack);
                    tridentEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0f, 2.5f + (float) j * 0.5f, 1.0f);
                    if (playerEntity.getAbilities().creativeMode) {
                        tridentEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                    }
                    world.spawnEntity(tridentEntity);
                    world.playSoundFromEntity(null, tridentEntity, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    if (!playerEntity.getAbilities().creativeMode) {
                        playerEntity.getInventory().removeOne(stack);
                    }
                }
            }
            playerEntity.incrementStat(Stats.USED.getOrCreateStat((TridentItem) (Object) this));
            if (j > 0) {
                float f = playerEntity.getYaw();
                float g = playerEntity.getPitch();
                float h = -MathHelper.sin(f * ((float) Math.PI / 180)) * MathHelper.cos(g * ((float) Math.PI / 180));
                float k = -MathHelper.sin(g * ((float) Math.PI / 180));
                float l = MathHelper.cos(f * ((float) Math.PI / 180)) * MathHelper.cos(g * ((float) Math.PI / 180));
                float m = MathHelper.sqrt(h * h + k * k + l * l);
                float n = 3.0f * ((1.0f + (float) j) / 4.0f);
                playerEntity.addVelocity(h *= n / m, k *= n / m, l *= n / m);
                playerEntity.useRiptide(20);
                if (playerEntity.isOnGround()) {
                    float o = 1.1999999f;
                    playerEntity.move(MovementType.SELF, new Vec3d(0.0, 1.1999999284744263, 0.0));
                }
                SoundEvent soundEvent = j >= 3 ? SoundEvents.ITEM_TRIDENT_RIPTIDE_3 : (j == 2 ? SoundEvents.ITEM_TRIDENT_RIPTIDE_2 : SoundEvents.ITEM_TRIDENT_RIPTIDE_1);
                world.playSoundFromEntity(null, playerEntity, soundEvent, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }
    }
}
