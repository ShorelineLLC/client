package net.shoreline.client.mixin.texture;

import net.minecraft.client.texture.NativeImage;
import net.shoreline.client.impl.imixin.INativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NativeImage.class)
public abstract class MixinNativeImage implements INativeImage
{
    @Override
    @Accessor("pointer")
    public abstract long getPointer();
}
