package net.shoreline.client.util;

import lombok.experimental.UtilityClass;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;

@UtilityClass
public class RegistryUtil
{
    private static final RegistryWrapper.WrapperLookup lookup = BuiltinRegistries.createWrapperLookup();

    public <T> RegistryEntry<T> getEntry(RegistryKey<T> key, RegistryKey<? extends Registry<T>> keys)
    {
        return lookup.getOrThrow(keys).getOptional(key).orElse(null);
    }
}
