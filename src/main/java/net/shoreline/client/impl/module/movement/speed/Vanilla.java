package net.shoreline.client.impl.module.movement.speed;

import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.module.movement.SpeedModule;

public class Vanilla extends BaseSpeedFeature<SpeedModule>
{
    public Vanilla()
    {
        super("Vanilla");
    }

    @Override
    public Vec3d onMoveUpdate(SpeedModule module, Vec3d currentMove)
    {
        double moveX = currentMove.x;
        double moveY = currentMove.y;
        double moveZ = currentMove.z;

        Vec2f motion = module.strafe(module.getSpeedConfig().getValue());
        moveX = motion.x;
        moveZ = motion.y;
        return new Vec3d(moveX, moveY, moveZ);
    }
}
