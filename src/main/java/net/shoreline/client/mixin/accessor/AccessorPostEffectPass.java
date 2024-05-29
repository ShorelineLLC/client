package net.shoreline.client.mixin.accessor;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PostEffectPass.class)
public interface AccessorPostEffectPass
{
    @Mutable
    @Accessor("input")
    void hookSetInput(Framebuffer framebuffer);

    @Mutable
    @Accessor("output")
    void hookSetOutput(Framebuffer framebuffer);
}
