package net.shoreline.client.impl.world;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.block.AsyncBlockScanner;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;

import java.util.Collection;
import java.util.List;

/**
 * Creates an immutable copy of the {@link net.minecraft.client.world.ClientWorld} world's block and entity states
 * @see EntityState
 */
public abstract class AsyncWorldScanner extends AsyncBlockScanner implements AsyncEntityView
{
    protected final AsyncEntityScanner entityScanner = new AsyncEntityScanner()
    {
        @Override
        protected float getRadius()
        {
            return AsyncWorldScanner.this.getRadius();
        }
    };

    public void createWorldLookup(ClientWorld world, ClientPlayerEntity player, boolean sphere)
    {
        createEntityLookup(world, player.getEyePos());
        if (sphere)
        {
            createSphere(world, BlockPos.ofFloored(player.getEyePos()));
        } else
        {
            createCube(world, BlockPos.ofFloored(player.getEyePos()));
        }
    }

    @Override
    public void createCube(ClientWorld world, BlockPos center)
    {
        super.createCube(world, center);
        createEntityLookup(world, center.toCenterPos());
    }

    @Override
    public void createSphere(ClientWorld world, BlockPos center)
    {
        super.createSphere(world, center);
        createEntityLookup(world, center.toCenterPos());
    }

    public void createEntityLookup(ClientWorld world, Vec3d center)
    {
        entityScanner.createEntityLookup(world, center);
    }

    @Override
    public LivingEntityState getLocalEntity()
    {
        return entityScanner.getLocalEntity();
    }

    @Override
    public EntityState getEntityById(int id)
    {
        return entityScanner.getEntityById(id);
    }

    @Override
    public List<EntityState> getOtherEntities(EntityState except, Box box)
    {
        return entityScanner.getOtherEntities(except, box);
    }

    @Override
    public Collection<EntityState> getEntities()
    {
        return entityScanner.getEntities();
    }

    @Override
    public Collection<LivingEntityState> getLivingEntities()
    {
        return entityScanner.getLivingEntities();
    }
}
