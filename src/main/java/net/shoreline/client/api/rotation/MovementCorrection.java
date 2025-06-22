package net.shoreline.client.api.rotation;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.impl.module.client.RotationsModule;

public class MovementCorrection
{
    public Vec2f correctMovement(float deltaYaw, float forward, float sideways)
    {
        float delta = deltaYaw * MathHelper.RADIANS_PER_DEGREE;
        float cos = MathHelper.cos(delta);
        float sin = MathHelper.sin(delta);
        float f = forward * cos + sideways * sin;
        float g = sideways * cos - forward * sin;
        if (RotationsModule.INSTANCE.isGrimMoveFix())
        {
            f = Math.round(f);
            g = Math.round(g);
        }
        return new Vec2f(g, f).normalize();
    }
}
