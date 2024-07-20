package net.shoreline.client.mixin.accessor;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface AccessorCamera
{
    @Accessor("cameraY")
    float getCameraY();

    @Accessor("lastCameraY")
    float getLastCameraY();
}
