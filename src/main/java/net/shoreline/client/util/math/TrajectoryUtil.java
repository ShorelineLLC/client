package net.shoreline.client.util.math;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.shoreline.client.impl.render.Interpolation;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class TrajectoryUtil
{
    // TODO: Trajectories for all projectiles.
    public List<Vec3d> getPearlTrajectory(Entity entity, int ticks)
    {
        List<Vec3d> result = new ArrayList<>();
        Vec3d interp = Interpolation.getRenderPosition(entity, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true));
        result.add(interp);

        Vec3d last;
        double x = interp.getX();
        double y = interp.getY();
        double z = interp.getZ();

        Vec3d velocity = entity.getVelocity();
        double mX = velocity.getX();
        double mY = velocity.getY();
        double mZ = velocity.getZ();

        // https://minecraft.wiki/w/Ender_Pearl
        double gravity = 0.03;
        float drag     = 0.99f;
        while (ticks-- >= 0)
        {
            last = new Vec3d(x, y, z);

            x += mX;
            y += mY;
            z += mZ;

            mX *= drag;
            mY = (mY * drag) - gravity;
            mZ *= drag;

            Vec3d position = new Vec3d(x, y, z);
            RaycastContext context = new RaycastContext(last, position, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity);
            BlockHitResult ray = MinecraftClient.getInstance().world.raycast(context);
            if (ray != null && ray.getType() == HitResult.Type.BLOCK)
            {
                return result;
            }

            result.add(position);
        }

        return result;
    }
}