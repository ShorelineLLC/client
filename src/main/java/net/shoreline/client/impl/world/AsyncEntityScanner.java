package net.shoreline.client.impl.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AsyncEntityScanner implements AsyncEntityView
{
    protected LivingEntityState localEntity;
    protected final ConcurrentMap<Integer, EntityState> entities = new ConcurrentHashMap<>();

    public void createEntityLookup(EntityView world, Vec3d c)
    {
        clearEntityLookup();
        float r2 = getRadius() * getRadius();
        Box box = new Box(c.x - getRadius(),
                c.y - getRadius(),
                c.z - getRadius(),
                c.x + getRadius(),
                c.y + getRadius(),
                c.z + getRadius());

        LivingEntity localPlayer = MinecraftClient.getInstance().player;
        for (Entity entity : world.getOtherEntities(localPlayer, box, e -> e.squaredDistanceTo(c) <= r2))
        {
            addEntityLookup(entity);
        }

        localEntity = new LivingEntityState(localPlayer);
    }

    public void addEntityLookup(Entity entity)
    {
        entities.put(entity.getId(), entity instanceof LivingEntity livingEntity ?
                new LivingEntityState(livingEntity) : new EntityState(entity));
    }

    public void clearEntityLookup()
    {
        entities.clear();
    }

    @Override
    public LivingEntityState getLocalEntity()
    {
        return localEntity;
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

    @Override
    public Collection<LivingEntityState> getLivingEntities()
    {
        return getEntities().stream()
            .filter(e -> e instanceof LivingEntityState)
            .map(e -> (LivingEntityState) e)
            .toList();
    }

    protected abstract float getRadius();
}
