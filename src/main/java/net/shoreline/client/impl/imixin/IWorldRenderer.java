package net.shoreline.client.impl.imixin;

import net.minecraft.client.render.Frustum;

@IMixin
public interface IWorldRenderer
{
    Frustum getFrustum();
}
