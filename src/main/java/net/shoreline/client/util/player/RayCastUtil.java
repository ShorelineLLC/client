package net.shoreline.client.util.player;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.shoreline.client.util.Globals;

/**
 * @author xgraza
 * @since 04/05/24
 */
public final class RayCastUtil implements Globals
{

    public static HitResult raycastEntity(final double reach, Vec3d position, final float[] angles)
    {
        Camera view = mc.gameRenderer.getCamera();
        Vec3d vec3d2 = RotationUtil.getRotationVector(angles[1], angles[0]);
        Vec3d vec3d3 = position.add(vec3d2.x * reach, vec3d2.y * reach, vec3d2.z * reach);
        Box box = new Box(position, position).stretch(vec3d2.multiply(reach)).expand(1.0, 1.0, 1.0);
        return ProjectileUtil.raycast(view.getFocusedEntity(), position, vec3d3, box, entity -> !entity.isSpectator() && entity.canHit(), reach * reach);
    }

    public static HitResult raycastEntity(final double reach)
    {
        Camera view = mc.gameRenderer.getCamera();
        Vec3d vec3d = view.getPos();
        Vec3d vec3d2 = RotationUtil.getRotationVector(view.getPitch(), view.getYaw());
        Vec3d vec3d3 = vec3d.add(vec3d2.x * reach, vec3d2.y * reach, vec3d2.z * reach);
        Box box = view.getFocusedEntity().getBoundingBox().stretch(vec3d2.multiply(reach)).expand(1.0, 1.0, 1.0);
        return ProjectileUtil.raycast(view.getFocusedEntity(), vec3d, vec3d3, box, entity -> !entity.isSpectator() && entity.canHit(), reach * reach);
    }

    public static HitResult rayCast(final double reach, final float[] angles)
    {
        final double eyeHeight = mc.player.getStandingEyeHeight();
        final Vec3d eyes = new Vec3d(mc.player.getX(), mc.player.getY() + eyeHeight, mc.player.getZ());
        return rayCast(reach, eyes, angles);
    }

    public static HitResult rayCast(final double reach, Vec3d position, final float[] angles)
    {
        // learn to give me real rotations
        if (Float.isNaN(angles[0]) || Float.isNaN(angles[1]))
        {
            return null;
        }

        final Vec3d rotationVector = RotationUtil.getRotationVector(angles[1], angles[0]);
        return mc.world.raycast(new RaycastContext(
                position,
                position.add(rotationVector.x * reach, rotationVector.y * reach, rotationVector.z * reach),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player));
    }
}
