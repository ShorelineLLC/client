package net.shoreline.client.impl.rotation;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

public class MovementCorrection
{
    public Vec2f correctMovement(boolean grimMoveFix, float deltaYaw, float forward, float sideways)
    {
        float delta = deltaYaw * MathHelper.RADIANS_PER_DEGREE;
        float cos = MathHelper.cos(delta);
        float sin = MathHelper.sin(delta);
        float f = forward * cos + sideways * sin;
        float g = sideways * cos - forward * sin;
        if (grimMoveFix)
        {
            f = Math.round(f);
            g = Math.round(g);
        }

        return new Vec2f(g, f);
    }
}
