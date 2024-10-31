package net.shoreline.loader.resource;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Allows resource loading by overwriting ModNioPack to dynamically open resources
 */
@SuppressWarnings("UnstableApiUsage")
public final class ResourcePackExt implements ResourcePack, ModResourcePack
{
    public static final Set<String> REGISTERED_SOUND_FILES = new HashSet<>();
    private final ModNioResourcePack parent;

    public ResourcePackExt(ModNioResourcePack parent)
    {
        this.parent = parent;
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
        String formattedName = String.format("assets/shoreline/%s", id.getPath());

        InputStream is;
        if ((is = (InputStream) getResourceInternal(formattedName)) != null)
        {
            return () -> is;
        }

        return this.parent.open(type, id);
    }

    private static native Object getResourceInternal(Object name);

    @Override
    public void findResources(ResourceType type,
                              String namespace,
                              String prefix,
                              ResultConsumer consumer)
    {
        if (prefix.equals("sounds"))
        {
            for (String soundFile : REGISTERED_SOUND_FILES)
            {
                String formattedName = String.format("assets/shoreline/sounds/%s", soundFile);

                InputStream is;
                if ((is = (InputStream) getResourceInternal(formattedName)) != null)
                {
                    Identifier id = Identifier.of("shoreline", String.format("sounds/%s", soundFile));
                    consumer.accept(id, () -> is);
                }
            }
        }

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
    public ResourcePackInfo getInfo()
    {
        return this.parent.getInfo();
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
