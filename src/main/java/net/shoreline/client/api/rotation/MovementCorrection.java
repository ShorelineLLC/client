package net.shoreline.client.api.rotation;

import net.minecraft.util.math.MathHelper;

public class MovementCorrection
{
    public float[] correctMovement(float deltaYaw, float forward, float sideways)
    {
        float delta = deltaYaw * MathHelper.RADIANS_PER_DEGREE;
        float cos = MathHelper.cos(delta);
        float sin = MathHelper.sin(delta);
        float f = forward * cos + sideways * sin;
        float g = sideways * cos - forward * sin;
        return new float[] { g, f };
    }
}
