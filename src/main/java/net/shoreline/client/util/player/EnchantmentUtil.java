package net.shoreline.client.util.player;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

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
