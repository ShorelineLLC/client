package net.shoreline.client.mixin.item;

import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shoreline.client.impl.event.item.TridentPullbackEvent;
import net.shoreline.client.impl.event.item.TridentWaterEvent;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TridentItem.class)
public abstract class MixinTridentItem implements Globals
{

    @Shadow
    public abstract int getMaxUseTime(ItemStack stack, LivingEntity user);

    @Shadow
    protected static boolean isAboutToBreak(ItemStack stack)
    {
        return false;
    }

    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    private void hookUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir)
    {
        TridentWaterEvent tridentWaterEvent = new TridentWaterEvent();
        EventBus.INSTANCE.dispatch(tridentWaterEvent);
        if (tridentWaterEvent.isCanceled())
        {
            cir.cancel();
            ItemStack itemStack = user.getStackInHand(hand);
            if (itemStack.getDamage() >= itemStack.getMaxDamage() - 1)
            {
                cir.setReturnValue(TypedActionResult.fail(itemStack));
                return;
            }
            user.setCurrentHand(hand);
            cir.setReturnValue(TypedActionResult.consume(itemStack));
        }
    }

    @Inject(method = "onStoppedUsing", at = @At(value = "HEAD"), cancellable = true)
    private void hookOnStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci)
    {
        if (!(user instanceof PlayerEntity playerEntity))
        {
            return;
        }
        int var6 = this.getMaxUseTime(stack, user) - remainingUseTicks;
        TridentPullbackEvent tridentPullbackEvent = new TridentPullbackEvent();
        EventBus.INSTANCE.dispatch(tridentPullbackEvent);
        if (!tridentPullbackEvent.isCanceled() && var6 < 10)
        {
            return;
        }
        TridentWaterEvent tridentWaterEvent = new TridentWaterEvent();
        EventBus.INSTANCE.dispatch(tridentWaterEvent);
        if (tridentWaterEvent.isCanceled())
        {
            ci.cancel();
            if (var6 >= 10)
            {
                float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, playerEntity);
                if (!(f > 0.0F) || playerEntity.isTouchingWaterOrRain()) {
                    if (!isAboutToBreak(stack)) {
                        RegistryEntry<SoundEvent> registryEntry = (RegistryEntry)EnchantmentHelper.getEffect(stack, EnchantmentEffectComponentTypes.TRIDENT_SOUND).orElse(SoundEvents.ITEM_TRIDENT_THROW);
                        if (!world.isClient) {
                            stack.damage(1, playerEntity, LivingEntity.getSlotForHand(user.getActiveHand()));
                            if (f == 0.0F) {
                                TridentEntity tridentEntity = new TridentEntity(world, playerEntity, stack);
                                tridentEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, 2.5F, 1.0F);
                                if (playerEntity.isInCreativeMode()) {
                                    tridentEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                                }

                                world.spawnEntity(tridentEntity);
                                world.playSoundFromEntity((PlayerEntity)null, tridentEntity, (SoundEvent)registryEntry.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                                if (!playerEntity.isInCreativeMode()) {
                                    playerEntity.getInventory().removeOne(stack);
                                }
                            }
                        }

                        playerEntity.incrementStat(Stats.USED.getOrCreateStat((TridentItem) (Object) this));
                        if (f > 0.0F)
                        {
                            float g = playerEntity.getYaw();
                            float h = playerEntity.getPitch();
                            float j = -MathHelper.sin(g * 0.017453292F) * MathHelper.cos(h * 0.017453292F);
                            float k = -MathHelper.sin(h * 0.017453292F);
                            float l = MathHelper.cos(g * 0.017453292F) * MathHelper.cos(h * 0.017453292F);
                            float m = MathHelper.sqrt(j * j + k * k + l * l);
                            j *= f / m;
                            k *= f / m;
                            l *= f / m;
                            playerEntity.addVelocity((double)j, (double)k, (double)l);
                            playerEntity.useRiptide(20, 8.0F, stack);
                            if (playerEntity.isOnGround())
                            {
                                float n = 1.1999999F;
                                playerEntity.move(MovementType.SELF, new Vec3d(0.0, 1.1999999284744263, 0.0));
                            }

                            world.playSoundFromEntity((PlayerEntity)null, playerEntity, (SoundEvent)registryEntry.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                        }
                    }
                }
            }
        }
    }
}
