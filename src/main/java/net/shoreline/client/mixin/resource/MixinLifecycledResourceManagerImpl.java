package net.shoreline.client.mixin.resource;

import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.ResourcePack;
import net.shoreline.loader.resource.ResourcePackExt;
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
    // to future bon from present bon:
    // if you're wondering why resources aren't loading, make sure the loader has an assets/shoreline folder
    // with something in it
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resource/NamespaceResourceManager;addPack(Lnet/minecraft/resource/ResourcePack;)V")
    )
    @SuppressWarnings("UnstableApiUsage")
    public void addPack(NamespaceResourceManager instance,
                        ResourcePack pack)
    {
        if (pack.getInfo().id().equals("shoreline"))
        {
            pack = new ResourcePackExt((ModNioResourcePack) pack);
        }

        instance.addPack(pack);
    }
}
