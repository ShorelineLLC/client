package net.shoreline.client.impl.rotation;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@UtilityClass
public class RotationUtil
{
    public float[] getRotationsTo(Vec3d src, Vec3d dest)
    {
        float yaw = (float) (Math.toDegrees(Math.atan2(dest.subtract(src).z,
                dest.subtract(src).x)) - 90.0f);
        float pitch = (float) Math.toDegrees(-Math.atan2(dest.subtract(src).y,
                Math.hypot(dest.subtract(src).x, dest.subtract(src).z)));

        float playerYaw = MinecraftClient.getInstance().player.getYaw();
        float playerPitch = MinecraftClient.getInstance().player.getPitch();

        float yaw1 = playerYaw + MathHelper.wrapDegrees(yaw - playerYaw);
        float pitch1 = playerPitch + MathHelper.wrapDegrees(pitch - playerPitch);

        return new float[] { yaw1, MathHelper.clamp(pitch1, -90.0f, 90.0f) };
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
