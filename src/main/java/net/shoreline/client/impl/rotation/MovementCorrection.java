package net.shoreline.client.impl.rotation;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.impl.module.client.AnticheatModule;

public class MovementCorrection
{
    private final AnticheatModule anticheat = AnticheatModule.INSTANCE;

    public Vec2f correctMovement(float deltaYaw, float forward, float sideways)
    {
        float delta = deltaYaw * MathHelper.RADIANS_PER_DEGREE;
        float cos = MathHelper.cos(delta);
        float sin = MathHelper.sin(delta);
        float f = forward * cos + sideways * sin;
        float g = sideways * cos - forward * sin;
        if (anticheat.getMoveFixConfig().getValue() == AnticheatModule.MoveFix.NORMAL)
        {
            f = Math.round(f);
            g = Math.round(g);
        }

        Vec2f vec2f = new Vec2f(g, f);
        return anticheat.getNormalizeMovement().getValue() ? vec2f.normalize() : vec2f;
    }
}
