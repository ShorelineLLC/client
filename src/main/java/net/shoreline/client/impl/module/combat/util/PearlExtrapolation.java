package net.shoreline.client.impl.module.combat.util;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.shoreline.client.impl.rotation.Rotation;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@UtilityClass
public class PearlExtrapolation
{
    public static final EntityDimensions PEARL_DIMENSIONS = EntityDimensions.changing(0.25f, 0.25f);

    public CopyOnWriteArrayList<Entity> extrapolateCollisions(Entity thrower, Rotation pearlRotation, int ticks)
    {
        World world = thrower.getWorld();
        CopyOnWriteArrayList<Entity> results = new CopyOnWriteArrayList<>();
        Set<Integer> seen = new HashSet<>();

        Vec3d throwPos = new Vec3d(thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ());
        double yr = Math.toRadians(pearlRotation.getYaw());
        double pr = Math.toRadians(pearlRotation.getPitch());
        double cp = Math.cos(pr);
        Vec3d dir = new Vec3d(-Math.sin(yr) * cp, -Math.sin(pr), Math.cos(yr) * cp).normalize();
        Vec3d vel = dir.multiply(EnderPearlItem.POWER).add(thrower.getVelocity());
        Vec3d prev = throwPos;
        double r = Math.max(PEARL_DIMENSIONS.width(), PEARL_DIMENSIONS.height()) * 0.5 + 0.1;

        for (int i = 0; i < ticks; i++)
        {
            Vec3d next = prev.add(vel);

            RaycastContext context = new RaycastContext(prev, next, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, thrower);
            HitResult blockHit = world.raycast(context);
            double blockDist = blockHit.getType() == HitResult.Type.BLOCK ? prev.squaredDistanceTo(blockHit.getPos()) : Double.POSITIVE_INFINITY;

            Box seg = new Box(prev, next).expand(r);

            for (Entity e : world.getOtherEntities(thrower, seg, en -> en instanceof LivingEntity && en.isAlive() && !en.isSpectator() && en != thrower))
            {
                if (e.getBoundingBox().contains(prev))
                {
                    continue;
                }

                var ip = e.getBoundingBox().expand(e.getTargetingMargin()).raycast(prev, next);
                if (ip.isPresent())
                {
                    double d = prev.squaredDistanceTo(ip.get());
                    if (d <= blockDist && seen.add(e.getId()))
                    {
                        results.add(e);
                    }
                }
            }

            if (blockDist != Double.POSITIVE_INFINITY)
            {
                break;
            }

            prev = next;
            vel = vel.multiply(0.99);
            vel = vel.add(0.0, -0.03, 0.0);
        }

        return results;
    }
}
