package net.shoreline.client.impl.world;

import lombok.Data;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Data
@Getter
public class EntityState
{
    private final String name;
    private final int id;
    private final EntityType<?> entityType;

    private final Vec3d pos;
    private final Vec3d eyePos;
    private final Vec3d velocity;
    private final Box boundingBox;
    private final EntityDimensions dimensions;

    private final boolean alive;
    private final boolean descending;
    private final int age;

    public EntityState(Entity entity)
    {
        this.name = entity.getName().getString();
        this.id = entity.getId();
        this.entityType = entity.getType();
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
        this.descending = entity.isDescending();
        this.age = entity.age;
    }

    public double getX()
    {
        return pos.getX();
    }

    public double getY()
    {
        return pos.getY();
    }

    public double getZ()
    {
        return pos.getZ();
    }

    public BlockPos getBlockPos()
    {
        return BlockPos.ofFloored(pos);
    }

    public double squaredDistanceTo(Vec3d pos)
    {
        return this.pos.squaredDistanceTo(pos);
    }

    /** YOU CAN ONLY CALL THE BELOW METHODS ON MC THREAD **/
    public Entity getEntity()
    {
        return MinecraftClient.getInstance().world.getEntityById(id);
    }

    public boolean isDead()
    {
        return getEntity() == null || !alive;
    }
}