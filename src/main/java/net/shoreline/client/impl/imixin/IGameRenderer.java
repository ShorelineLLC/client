package net.shoreline.client.impl.imixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.Pool;

public interface IGameRenderer
{
    Pool getPool();

    float invokeGetFov(Camera camera, float tickDelta, boolean changingFov);
}
