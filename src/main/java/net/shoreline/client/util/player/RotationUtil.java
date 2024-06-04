package net.shoreline.client.util.player;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.util.Globals;

/**
 * @author linus
 * @since 1.0
 */
public class RotationUtil implements Globals
{
    /**
     * @param src
     * @param dest
     * @return
     */
    public static float[] getRotationsTo(Vec3d src, Vec3d dest)
    {
        float yaw = (float) (Math.toDegrees(Math.atan2(dest.subtract(src).z,
                dest.subtract(src).x)) - 90);
        float pitch = (float) Math.toDegrees(-Math.atan2(dest.subtract(src).y,
                Math.hypot(dest.subtract(src).x, dest.subtract(src).z)));
        return new float[]
                {
                        MathHelper.wrapDegrees(yaw),
                        MathHelper.wrapDegrees(pitch)
                };
    }

    public static float[] smooth(float[] target, float[] previous, float rotationSpeed)
    {
        float speed = (1.0f - (MathHelper.clamp(rotationSpeed / 100.0f, 0.1f, 0.9f))) * 10.0f;

        float[] rotations = new float[2];

        rotations[0] = previous[0] + (float) (-getAngleDifference(previous[0], target[0]) / speed);
        rotations[1] = previous[1] + (-(previous[1] - target[1]) / speed);

        // force pitch to be in between -90 and 90 (instant ac-ban on some acs)
        rotations[1] = MathHelper.clamp(rotations[1], -90.0f, 90.0f);

        return rotations;
    }

    public static double getAngleDifference(float client, float yaw)
    {
        return ((client - yaw) % 360.0 + 540.0) % 360.0 - 180.0;
    }

    /**
     * Gets the difference between two pitch angles
     *
     * @param client the current pitch value
     * @param pitch  the target pitch value
     * @return the difference between the two angles
     */
    public static double getAnglePitchDifference(float client, float pitch)
    {
        return ((client - pitch) % 180.0 + 270.0) % 180.0 - 90.0;
    }

    /**
     * @param pitch
     * @param yaw
     * @return
     */
    public static Vec3d getRotationVector(float pitch, float yaw)
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
