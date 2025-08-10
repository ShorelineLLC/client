package net.shoreline.client.impl.world;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.block.AsyncBlockScanner;
import net.shoreline.client.impl.module.combat.util.DamageUtil;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Creates an immutable copy of the {@link net.minecraft.client.world.ClientWorld} world's block and entity states
 * @see EntityState
 */
public abstract class AsyncWorldScanner extends AsyncBlockScanner
{
    private final ConcurrentMap<Integer, EntityState> entities = new ConcurrentHashMap<>();

    @Override
    public void createCube(ClientWorld world, BlockPos center)
    {
        super.createCube(world, center);

        entities.clear();
        Vec3d c = center.toCenterPos();
        Box box = new Box(c.x - getRadius(),
                c.y - getRadius(),
                c.z - getRadius(),
                c.x + getRadius(),
                c.y + getRadius(),
                c.z + getRadius());

        for (Entity entity : world.getOtherEntities(null, box))
        {
            if (entity instanceof LivingEntity e)
            {
                entities.put(e.getId(), new EntityState(e.getPos(), DamageUtil.getHealth(e)));
            }
        }
    }

    @Override
    public void createSphere(ClientWorld world, BlockPos center)
    {
        super.createCube(world, center);

        entities.clear();
        int r2 = getRadius() * getRadius();
        Vec3d c = center.toCenterPos();
        Box box = new Box(c.x - getRadius(),
                c.y - getRadius(),
                c.z - getRadius(),
                c.x + getRadius(),
                c.y + getRadius(),
                c.z + getRadius());

        for (Entity entity : world.getOtherEntities(null, box, e -> e.squaredDistanceTo(c) <= r2))
        {
            if (entity instanceof LivingEntity e)
            {
                entities.put(e.getId(), new EntityState(e.getPos(), DamageUtil.getHealth(e)));
            }
        }
    }

    public EntityState getEntityById(int id)
    {
        return entities.get(id);
    }

    public Collection<EntityState> getEntities()
    {
        return entities.values();
    }
}
