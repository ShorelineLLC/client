package net.shoreline.client.util.player;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public class EnchantmentUtil
{

    public static int getLevel(ItemStack stack, RegistryKey<Enchantment> enchantmentRegistryKey)
    {
        if (!stack.getComponents().contains(DataComponentTypes.ENCHANTMENTS))
        {
            return 0;
        }
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> e : stack.getComponents()
                .get(DataComponentTypes.ENCHANTMENTS).getEnchantmentEntries())
        {
            if (e.getKey().getKey().isPresent() && e.getKey().getKey().get().equals(enchantmentRegistryKey))
            {
                return e.getIntValue();
            }
        }
        return 0;
    }
}
