package net.shoreline.client.util.item;

import lombok.experimental.UtilityClass;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@UtilityClass
public class ItemUtil
{
    public boolean isSameItem(ItemStack stack1, ItemStack stack2)
    {
        return stack1.getItem().equals(stack2.getItem()) && stack1.getName().equals(stack2.getName());
    }

    public boolean isTool(Item item)
    {
        return item.getTranslationKey().contains("shovel") || item.getTranslationKey().contains("axe")
                || item.getTranslationKey().contains("pickaxe");
    }

    public boolean isSword(Item item)
    {
        return item.getTranslationKey().contains("sword");
    }

    public float getStackPercent(ItemStack stack)
    {
        return (float) getDurability(stack) / stack.getMaxDamage();
    }

    public int getDurability(ItemStack stack)
    {
        return stack.getMaxDamage() - stack.getDamage();
    }
}
