package net.shoreline.client.impl.world;

import lombok.Data;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Data
@Getter
public class EntityState
{
    private final Entity entity;
    private final int id;

    private final Vec3d pos;
    private final Vec3d eyePos;
    private final Vec3d velocity;
    private final Box boundingBox;
    private final EntityDimensions dimensions;

    private final boolean alive;
    private final float totalHealth;
    private final int age;

    public EntityState(Entity entity)
    {
        this.entity = entity;
        this.id = entity.getId();
        this.pos = entity.getPos();
        this.eyePos = entity.getEyePos();
        this.velocity = new Vec3d(
                entity.getX() - entity.prevX,
                entity.getY() - entity.prevY,
                entity.getZ() - entity.prevZ
        );

        this.boundingBox = entity.getBoundingBox();
        this.dimensions = entity.getDimensions(entity.getPose());
        this.alive = entity.isAlive();
        this.totalHealth = entity instanceof LivingEntity e ? e.getHealth() + e.getAbsorptionAmount() : 0.0f;
        this.age = entity.age;
    }

    public BlockPos getBlockPos()
    {
        return BlockPos.ofFloored(pos);
    }

    public double squaredDistanceTo(Vec3d pos)
    {
        return this.pos.squaredDistanceTo(pos);
    }
}