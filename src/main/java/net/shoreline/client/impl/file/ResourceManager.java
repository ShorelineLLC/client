package net.shoreline.client.impl.file;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.util.Identifier;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.GenericFeature;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceManager extends GenericFeature
{
    public ResourceManager()
    {
        super("Resource Packs");
        loadResourcePack("lava", ResourcePackActivationType.DEFAULT_ENABLED);
    }

    public void loadResourcePack(String packName, ResourcePackActivationType type)
    {
        ModContainer container = FabricLoader.getInstance()
                .getModContainer(ShorelineMod.MOD_ID)
                .orElseThrow(() -> new IllegalStateException("Missing mod container: " + ShorelineMod.MOD_ID));

        ResourceManagerHelper.registerBuiltinResourcePack(
                Identifier.of(ShorelineMod.MOD_ID, packName),
                container,
                type
        );
    }

    public void toggleResourcePack(String packName, boolean enable)
    {
        mc.execute(() ->
        {
            ResourcePackManager resourcePackManager = mc.getResourcePackManager();

            Set<String> enabled = resourcePackManager.getEnabledProfiles().stream()
                    .map(p -> p.getDisplayName().getString())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (enable)
            {
                ResourcePackProfile profile = resourcePackManager.getProfile(packName);
                if (profile == null)
                {
                    return;
                }

                enabled.add(packName);
            } else
            {
                enabled.remove(packName);
            }

            resourcePackManager.setEnabledProfiles(List.copyOf(enabled));
            mc.options.write();
            mc.reloadResources();
        });
    }
}
