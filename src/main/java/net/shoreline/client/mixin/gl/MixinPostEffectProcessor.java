package net.shoreline.client.mixin.gl;

import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.shoreline.client.impl.imixin.IPostEffectProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PostEffectProcessor.class)
public abstract class MixinPostEffectProcessor implements IPostEffectProcessor
{
    @Override
    @Accessor("passes")
    public abstract List<PostEffectPass> getPasses();
}
