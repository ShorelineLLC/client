package net.shoreline.client.util.world;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.client.impl.module.render.FreecamModule;

import java.util.Optional;

@UtilityClass
public class RaytraceUtil
{
    public HitResult raycast(final Entity viewEntity,
                             final double reach,
                             Vec3d position,
                             final float[] angles)
    {
        return Optional.ofNullable(raycastFromEntity(viewEntity, reach, position, angles)).orElseGet(() -> raycast(reach, position, angles));
    }

    public HitResult raycast(final double reach, Vec3d position, final float[] angles)
    {
        final Vec3d rotationVector = RotationUtil.getRotationVector(angles[0], angles[1]);
        return MinecraftClient.getInstance().world.raycast(new RaycastContext(
                position,
                position.add(rotationVector.x * reach, rotationVector.y * reach, rotationVector.z * reach),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                MinecraftClient.getInstance().player));
    }

    public HitResult raycastFromCamera(final double reach)
    {
        Camera view = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d viewPos = FreecamModule.INSTANCE.isEnabled() ? FreecamModule.INSTANCE.getPosition() : view.getPos();
        float[] viewRot = FreecamModule.INSTANCE.isEnabled() ?
                new float[] { FreecamModule.INSTANCE.getYaw(), FreecamModule.INSTANCE.getPitch() } :
                new float[] { view.getYaw(), view.getPitch() };

        return raycastFromEntity(view.getFocusedEntity(), reach, viewPos, viewRot);
    }

    public HitResult raycastFromEntity(final Entity viewEntity,
                                       final double reach,
                                       final Vec3d position,
                                       final float[] angles)
    {
        Vec3d vec3d2 = RotationUtil.getRotationVector(angles[0], angles[1]);
        Vec3d vec3d3 = position.add(vec3d2.x * reach, vec3d2.y * reach, vec3d2.z * reach);
        Box box = Box.of(position, 0.0, 0.0, 0.0).stretch(vec3d2.multiply(reach)).expand(1.0, 1.0, 1.0);
        return ProjectileUtil.raycast(viewEntity, position, vec3d3, box, entity -> !entity.isSpectator() && entity.canHit(), reach * reach);
    }

    public boolean canSee(Vec3d toSee, Entity entity)
    {
        RaycastContext context = new RaycastContext(entity.getEyePos(), toSee, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
        return MinecraftClient.getInstance().world.raycast(context).getType() == HitResult.Type.MISS;
    }
}
