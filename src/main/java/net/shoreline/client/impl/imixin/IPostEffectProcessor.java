package net.shoreline.client.impl.imixin;

import net.minecraft.client.gl.PostEffectPass;

import java.util.List;

@IMixin
public interface IPostEffectProcessor
{
    List<PostEffectPass> getPasses();
}
