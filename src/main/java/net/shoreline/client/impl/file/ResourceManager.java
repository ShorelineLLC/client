package net.shoreline.client.impl.file;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;
import net.shoreline.client.ShorelineMod;

public class ResourceManager
{
    public ResourceManager()
    {
        loadResourcePack("lava");
    }

    public void loadResourcePack(String packName)
    {
        ModContainer container = FabricLoader.getInstance()
                .getModContainer(ShorelineMod.MOD_ID)
                .orElseThrow(() -> new IllegalStateException("Missing mod container: " + ShorelineMod.MOD_ID));

        ResourceManagerHelper.registerBuiltinResourcePack(
                Identifier.of(ShorelineMod.MOD_ID, packName),
                container,
                ResourcePackActivationType.DEFAULT_ENABLED
        );
    }
}
