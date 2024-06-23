package net.shoreline.loader.impl.classloading;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.shoreline.loader.Natives;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public final class ShorelineResourcePack implements ResourcePack, ModResourcePack
{
    private final ModNioResourcePack parent;

    public ShorelineResourcePack(ModNioResourcePack parent)
    {
        this.parent = parent;
    }

    @Override
    public boolean isAlwaysStable()
    {
        return this.parent.isAlwaysStable();
    }

    @Nullable
    @Override
    public InputSupplier<InputStream> openRoot(String... segments)
    {
        return this.parent.openRoot(segments);
    }

    @Nullable
    @Override
    public InputSupplier<InputStream> open(ResourceType type,
                                           Identifier id)
    {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
        {
            String formattedName = String.format("assets/shoreline/%s", id.getPath());
            byte[] content = (byte[]) Natives.k(formattedName);

            if (content == null)
            {
                return null;
            }

            return () -> new ByteArrayInputStream(content);
        }

        return this.parent.open(type, id);
    }

    @Override
    public void findResources(ResourceType type,
                              String namespace,
                              String prefix,
                              ResultConsumer consumer)
    {
        this.parent.findResources(type, namespace, prefix, consumer);
    }

    @Override
    public Set<String> getNamespaces(ResourceType type)
    {
        return this.parent.getNamespaces(type);
    }

    @Nullable
    @Override
    public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException
    {
        return this.parent.parseMetadata(metaReader);
    }

    @Override
    public String getName()
    {
        return this.parent.getName();
    }

    @Override
    public ModMetadata getFabricModMetadata()
    {
        return this.parent.getFabricModMetadata();
    }

    @Override
    public ModResourcePack createOverlay(String overlay)
    {
        return this.parent.createOverlay(overlay);
    }

    @Override
    public void close()
    {
        this.parent.close();
    }
}
