package net.shoreline.client.impl.player.rotation;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@UtilityClass
public class RotationUtil
{
    public float[] getRotationsTo(Vec3d src, Vec3d dest)
    {
        float yaw = (float) (Math.toDegrees(Math.atan2(dest.subtract(src).z,
                dest.subtract(src).x)) - 90);
        float pitch = (float) Math.toDegrees(-Math.atan2(dest.subtract(src).y,
                Math.hypot(dest.subtract(src).x, dest.subtract(src).z)));
        return new float[] { MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch) };
    }

    public Vec3d getRotationVector(float yaw, float pitch)
    {
        float f = pitch * ((float) Math.PI / 180.0f);
        float g = -yaw * ((float) Math.PI / 180.0f);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }
}
