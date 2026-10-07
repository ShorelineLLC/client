package net.shoreline.client.impl.world;

import net.minecraft.util.math.Box;

import java.util.Collection;

public interface AsyncEntityView
{
    LivingEntityState getLocalEntity();

    EntityState getEntityById(int id);

    Collection<EntityState> getOtherEntities(EntityState except, Box box);

    Collection<EntityState> getEntities();

    Collection<LivingEntityState> getLivingEntities();
}
