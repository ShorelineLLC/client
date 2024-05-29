package net.shoreline.client.impl.imixin;

import net.minecraft.client.gl.Framebuffer;

@IMixin
public interface IPostEffectProcessor {

    void overwriteBuffer(String name, Framebuffer buffer);
}
