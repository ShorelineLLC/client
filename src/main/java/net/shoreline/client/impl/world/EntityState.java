package net.shoreline.client.impl.world;

import lombok.Data;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Data
@Getter
public class EntityState
{
    private final Entity entity;

    private final Vec3d pos;
    private final Vec3d eyePos;
    private final Box boundingBox;

    private final float totalHealth;
    private final int age;

    public EntityState(Entity entity)
    {
        this.entity = entity;
        this.pos = entity.getPos();
        this.eyePos = entity.getEyePos();
        this.boundingBox = entity.getBoundingBox();
        this.totalHealth = entity instanceof LivingEntity e ? e.getHealth() + e.getAbsorptionAmount() : 0.0f;
        this.age = entity.age;
    }

    public double squaredDistanceTo(Vec3d pos)
    {
        return this.pos.squaredDistanceTo(pos);
    }
}