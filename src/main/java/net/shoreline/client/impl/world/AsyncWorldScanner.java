package net.shoreline.client.impl.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.block.AsyncBlockScanner;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Creates an immutable copy of the {@link net.minecraft.client.world.ClientWorld} world's block and entity states
 * @see EntityState
 */
public abstract class AsyncWorldScanner extends AsyncBlockScanner implements AsyncEntityView
{
    protected EntityState localEntity;
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

        LivingEntity localPlayer = MinecraftClient.getInstance().player;
        for (Entity entity : world.getOtherEntities(localPlayer, box))
        {
            entities.put(entity.getId(), new EntityState(entity));
        }

        localEntity = new EntityState(localPlayer);
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

        LivingEntity localPlayer = MinecraftClient.getInstance().player;
        for (Entity entity : world.getOtherEntities(localPlayer, box, e -> e.squaredDistanceTo(c) <= r2))
        {
            entities.put(entity.getId(), new EntityState(entity));
        }

        localEntity = new EntityState(localPlayer);
    }

    @Override
    public EntityState getEntityById(int id)
    {
        return entities.get(id);
    }

    @Override
    public List<EntityState> getOtherEntities(EntityState except, Box box)
    {
        List<EntityState> otherEntities = new CopyOnWriteArrayList<>();
        for (EntityState state : getEntities())
        {
            if (state.equals(except))
            {
                continue;
            }

            if (state.getBoundingBox().intersects(box))
            {
                otherEntities.add(state);
            }
        }

        return otherEntities;
    }

    @Override
    public Collection<EntityState> getEntities()
    {
        return entities.values();
    }
}
