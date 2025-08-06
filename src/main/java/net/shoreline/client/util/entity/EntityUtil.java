package net.shoreline.client.util.entity;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

@UtilityClass
public class EntityUtil
{
    public BlockPos getRoundedBlockPos(Entity entity)
    {
        return BlockPos.ofFloored(entity.getBlockX(), Math.round(entity.getY()), entity.getBlockZ());
    }

    public ItemStack[] getEquippedItems(LivingEntity entity)
    {
        return new ItemStack[]
                {
                        entity.getEquippedStack(EquipmentSlot.MAINHAND),
                        entity.getEquippedStack(EquipmentSlot.OFFHAND),
                        entity.getEquippedStack(EquipmentSlot.HEAD),
                        entity.getEquippedStack(EquipmentSlot.BODY),
                        entity.getEquippedStack(EquipmentSlot.LEGS),
                        entity.getEquippedStack(EquipmentSlot.FEET)
                };
    }
}
