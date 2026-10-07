package net.shoreline.client.util.entity;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class EntityUtil
{
    public boolean isHostile(final Entity entity)
    {
        return entity instanceof Monster && !isNeutral(entity);
    }

    public boolean isPassive(Entity entity)
    {
        return entity instanceof PassiveEntity || entity instanceof AmbientEntity || entity instanceof SquidEntity;
    }

    public boolean isHostile(EntityType<?> type)
    {
        return type.getSpawnGroup() == SpawnGroup.MONSTER;
    }

    public boolean isPassive(EntityType<?> type)
    {
        SpawnGroup group = type.getSpawnGroup();
        return group == SpawnGroup.CREATURE || group == SpawnGroup.AMBIENT
                || type == EntityType.SQUID || type == EntityType.GLOW_SQUID;
    }

    private boolean isNeutral(Entity entity)
    {
        return entity instanceof EndermanEntity enderman && !enderman.isAttacking()
                || entity instanceof ZombifiedPiglinEntity piglin && !piglin.isAttacking()
                || entity instanceof WolfEntity wolf && !wolf.isAttacking()
                || entity instanceof IronGolemEntity ironGolem && !ironGolem.isAttacking()
                || entity instanceof BeeEntity bee && !bee.isAttacking();
    }

    public BlockPos getRoundedBlockPos(Entity entity)
    {
        return BlockPos.ofFloored(entity.getBlockX(), Math.round(entity.getY()), entity.getBlockZ());
    }

    public List<ItemStack> getEquippedItems(LivingEntity entity)
    {
        final List<ItemStack> stacks = new ArrayList<>();
        EquipmentSlot.VALUES.forEach(equipmentSlot -> stacks.add(entity.getEquippedStack(equipmentSlot)));
        return stacks;
    }
}
