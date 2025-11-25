package net.shoreline.client.impl.world;

import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;

@Getter
public class LivingEntityState extends EntityState
{
    private final float totalHealth;
    private final int totalArmor;

    private final Item heldItem;

    public LivingEntityState(LivingEntity livingEntity)
    {
        super(livingEntity);
        this.totalHealth = livingEntity.getHealth() + livingEntity.getAbsorptionAmount();
        this.totalArmor = livingEntity.getArmor();
        this.heldItem = livingEntity.getMainHandStack().getItem();
    }
}
