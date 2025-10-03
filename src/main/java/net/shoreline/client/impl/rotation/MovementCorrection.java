package net.shoreline.client.impl.rotation;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.impl.module.client.RotationsModule;
import net.shoreline.client.impl.module.client.RotationsModule.MoveFix;

public class MovementCorrection
{
    private final RotationsModule rotationConfig = RotationsModule.INSTANCE;

    public Vec2f correctMovement(float deltaYaw, float forward, float sideways)
    {
        float delta = deltaYaw * MathHelper.RADIANS_PER_DEGREE;
        float cos = MathHelper.cos(delta);
        float sin = MathHelper.sin(delta);
        float f = forward * cos + sideways * sin;
        float g = sideways * cos - forward * sin;
        if (rotationConfig.getMoveFixConfig().getValue() == MoveFix.NORMAL)
        {
            f = Math.round(f);
            g = Math.round(g);
        }

        Vec2f vec2f = new Vec2f(g, f);
        return rotationConfig.getNormalizeMovement().getValue() ? vec2f.normalize() : vec2f;
    }
}
