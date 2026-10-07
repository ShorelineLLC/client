package net.shoreline.client.util.entity;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.world.explosion.ExplosionTrace;
import net.shoreline.client.util.item.EnchantUtil;
import net.shoreline.client.impl.world.explosion.ExplosionUtil;
import net.shoreline.client.util.item.ItemUtil;

import java.util.Collections;
import java.util.Optional;

public class DamageableFakePlayer extends FakePlayerEntity
{
    public DamageableFakePlayer(PlayerEntity player, String name)
    {
        super(player, name);
        setAbsorptionHealth(player.getAbsorptionAmount());
    }

    @Override
    public void baseTick()
    {
        super.baseTick();
        for (StatusEffectInstance effectInstance : getStatusEffects())
        {
            StatusEffect effect = effectInstance.getEffectType().value();
            if (!effect.canApplyUpdateEffect(effectInstance.getDuration(), effectInstance.getAmplifier()))
            {
                continue;
            }

            effect.applyUpdateEffect(null, this, effectInstance.getAmplifier());
        }
    }

    public void simulateAttackFrom(ClientWorld world, PlayerEntity entity)
    {
        float f = (float) entity.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        ItemStack itemStack = entity.getStackInHand(Hand.MAIN_HAND);
        DamageSource damageSource = Optional.ofNullable(itemStack.getItem().getDamageSource(entity)).orElse(entity.getDamageSources().playerAttack(entity));

        int sharpnessLevel = EnchantUtil.getLevel(Enchantments.SHARPNESS, itemStack);
        f += 1.0f + 0.5f * sharpnessLevel;

        if (entity.hasStatusEffect(StatusEffects.STRENGTH))
        {
            int amp = entity.getStatusEffect(StatusEffects.STRENGTH).getAmplifier();
            f += 3.0f * (amp + 1);
        }

        if (entity.hasStatusEffect(StatusEffects.WEAKNESS))
        {
            int amp = entity.getStatusEffect(StatusEffects.WEAKNESS).getAmplifier();
            f -= 4.0f * (amp + 1);
        }

        float h = entity.getAttackCooldownProgress(0.5f);
        f *= 0.2f + h * h * 0.8f;

        boolean critAttack = h > 0.9f && entity.getVelocity().y < 0.0
                && !entity.isOnGround()
                && !entity.isClimbing()
                && !entity.isTouchingWater()
                && !entity.hasStatusEffect(StatusEffects.BLINDNESS)
                && !entity.hasVehicle()
                && !entity.isSprinting();

        if (critAttack)
        {
            f *= 1.5f;
            world.playSound(entity, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, entity.getSoundCategory(), 1.0f, 1.0f);
            entity.addCritParticles(this);
        } else
        {
            if (h > 0.9f)
            {
                world.playSound(entity, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, entity.getSoundCategory(), 1.0f, 1.0f);
            } else
            {
                world.playSound(entity, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, entity.getSoundCategory(), 1.0f, 1.0f);
            }
        }

        if (itemStack.isIn(ItemTags.SWORDS) && itemStack.hasEnchantments())
        {
            entity.addEnchantedHitParticles(this);
        }

        damage(world, damageSource, f);
    }

    public void simulateExplosionFrom(ClientWorld world, Vec3d vec3d)
    {
        float damage = ExplosionTrace.getDamageToPos(world,
                vec3d,
                getPos(),
                getBoundingBox(),
                12.0f,
                false,
                Collections.emptySet());

        float applied = ExplosionUtil.getAppliedDamageToEntity(this, damage);
        damage(world, getDamageSources().explosion(null), applied);

//        if (this.hurtTime < 8.0f)
//        {
//            for (ItemStack stack : getArmorItems())
//            {
//                int armorDamage = ExplosionUtil.getArmorDurabilityDamage(stack, damage);
//                if (ItemUtil.getDurability(stack) - armorDamage <= 0)
//                {
//                    stack.setDamage(0);
//                    continue;
//                }
//
//                stack.setDamage(stack.getDamage() + armorDamage);
//            }
//        }
    }

    protected void damage(ClientWorld world, DamageSource source, float amount)
    {
        if (source.isScaledWithDifficulty())
        {
            amount = amount * 3.0f / 2.0f;
        }

        if (amount == 0.0f)
        {
            return;
        }

        this.limbAnimator.setSpeed(1.5f);

        if (this.timeUntilRegen > 10.0f && !source.isIn(DamageTypeTags.BYPASSES_COOLDOWN))
        {
            if (amount <= this.lastDamageTaken)
            {
                return;
            }

            applyDamage(source, amount - this.lastDamageTaken);
            this.lastDamageTaken = amount;
        } else
        {
            this.lastDamageTaken = amount;
            this.timeUntilRegen = 15;
            applyDamage(source, amount);
            this.hurtTime = 10;
            this.maxHurtTime = 10;
            playHurtSound(source);
        }

        this.lastDamageSource = source;
        this.lastDamageTime = world.getTime();
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource source)
    {
        return false;
    }

    @Override
    public void setVelocityClient(double x, double y, double z)
    {
        // cancel client velocity
    }

    // Dumb fix
    public void setAbsorptionHealth(float absorptionAmount)
    {
        super.setAbsorptionAmount(absorptionAmount);
        getDataTracker().set(PlayerEntity.ABSORPTION_AMOUNT, absorptionAmount);
    }

    @Override
    public float getAbsorptionAmount()
    {
        return getDataTracker().get(PlayerEntity.ABSORPTION_AMOUNT);
    }

    private void applyDamage(DamageSource source, float amount)
    {
        if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR))
        {
            amount = DamageUtil.getDamageLeft(this, amount, source, getArmor(), (float) getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));
        }

        float f = amount = modifyAppliedDamage(source, amount);
        amount = Math.max(amount - getAbsorptionAmount(), 0.0f);
        setAbsorptionHealth(getAbsorptionAmount() - (f - amount));
        if (amount == 0.0f)
        {
            return;
        }

        float health = getHealth() - amount;
        if (health <= 0.0f)
        {
            simulateTotemPop();
            return;
        }

        setHealth(health);
    }

    public void simulateGappleEat()
    {
        addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 400, 1));
        addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 6000, 0));
        addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 6000, 0));
        addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 3));

        setAbsorptionHealth(16.0f);
    }

    public void simulateTotemPop()
    {
        setHealth(1.0f);
        clearStatusEffects();

        addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
        addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
        setAbsorptionHealth(8.0f);

        Managers.NETWORK.receivePacket(new EntityStatusS2CPacket(this, EntityStatuses.USE_TOTEM_OF_UNDYING));
    }
}