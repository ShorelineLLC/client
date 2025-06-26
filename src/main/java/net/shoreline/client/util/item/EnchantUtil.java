package net.shoreline.client.util.item;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

@UtilityClass
public class EnchantUtil {

    public int getLevel(RegistryKey<Enchantment> key, ItemStack stack)
    {
        RegistryWrapper.WrapperLookup registryLookup = MinecraftClient.getInstance().world.getRegistryManager();
        RegistryWrapper<Enchantment> enchantmentWrapper = registryLookup.getOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> entry = enchantmentWrapper.getOptional(key).orElse(null);
        return EnchantmentHelper.getLevel(entry, stack);
    }

}
