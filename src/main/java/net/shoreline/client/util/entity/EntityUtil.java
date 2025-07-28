package net.shoreline.client.util.entity;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@UtilityClass
public class EntityUtil
{
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
