package net.shoreline.client.mixin.accessor;

import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NativeImage.class)
public interface AccessorNativeImage
{

    @Accessor("pointer")
    long hookGetPointer();
}
