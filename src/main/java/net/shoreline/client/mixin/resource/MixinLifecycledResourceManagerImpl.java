package net.shoreline.client.mixin.resource;

import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.ResourcePack;
import net.shoreline.loader.Loader;
import net.shoreline.loader.impl.classloading.ClassLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Injects our custom resource manager to allow for remote resource loading (out of dev environments)
 *
 * @author bon
 */
@Mixin(LifecycledResourceManagerImpl.class)
public final class MixinLifecycledResourceManagerImpl
{
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resource/NamespaceResourceManager;addPack(Lnet/minecraft/resource/ResourcePack;)V")
    )
    public void addPack(NamespaceResourceManager instance,
                        ResourcePack pack)
    {
        if (pack.getName().equals("shoreline"))
        {
            pack = ClassLoader.transformPack(pack);
            Loader.LOGGER.info("Loaded resources!");
        }

        instance.addPack(pack);
    }
}
