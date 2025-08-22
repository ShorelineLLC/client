package net.shoreline.client.util.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Set;

@UtilityClass
public class EnchantUtil
{
    public RegistryEntry<Enchantment> getEntry(RegistryKey<Enchantment> key)
    {
        RegistryWrapper.WrapperLookup mcLookup = MinecraftClient.getInstance().world.getRegistryManager();
        return mcLookup.getOrThrow(RegistryKeys.ENCHANTMENT).getOptional(key).orElse(null);
    }

    public int getLevel(RegistryKey<Enchantment> key, ItemStack stack)
    {
        RegistryEntry<Enchantment> entry = getEntry(key);
        return EnchantmentHelper.getLevel(entry, stack);
    }

    public boolean isEnchantsObfuscated(ItemStack itemStack)
    {
        Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> enchants =
                EnchantmentHelper.getEnchantments(itemStack).getEnchantmentEntries();

        if (enchants.size() > 1)
        {
            return false;
        }

        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> e : enchants)
        {
            RegistryEntry<Enchantment> enchantment = e.getKey();
            int lvl = e.getIntValue();
            if (lvl == 0 && enchantment.getKey().isPresent() && enchantment.getKey().get() == Enchantments.PROTECTION)
            {
                return true;
            }
        }
        return false;
    }
}
