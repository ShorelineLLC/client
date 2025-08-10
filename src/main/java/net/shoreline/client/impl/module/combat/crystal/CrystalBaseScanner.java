package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.world.AsyncWorldScanner;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.explosion.ExplosionTrace;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CrystalBaseScanner extends AsyncWorldScanner
{
    private static final Box FULL_CRYSTAL_BB = new Box(0.0, 0.0, 0.0, 1.0, 2.0, 1.0);
    private static final Box HALF_CRYSTAL_BB = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

    private final AutoCrystalModule autoCrystal = AutoCrystalModule.INSTANCE;

    private final List<CrystalData<BlockPos>> crystalBases = new CopyOnWriteArrayList<>();

    @Override
    protected void visit(BlockPos pos, BlockState state)
    {
        if (!canUseOnBlock(pos, state))
        {
            return;
        }

        Vec3d explosionCenter = pos.toBottomCenterPos().add(0.0, 1.0, 0.0);
        float local = getCrystalDamage(explosionCenter, localEntity);

        boolean willKillPlayer = localEntity.getTotalHealth() - local < 0.5f;
        if (local > autoCrystal.getMaxSelfDamage().getValue() || willKillPlayer)
        {
            return;
        }

        for (EntityState entity : getEntities())
        {
            if (!(entity.getEntity() instanceof LivingEntity))
            {
                continue;
            }

            double blockDist = pos.getSquaredDistance(entity.getPos());
            if (blockDist > 144.0f)
            {
                continue;
            }

            float targetRange = autoCrystal.getTargetRange().getValue();
            double dist = localEntity.squaredDistanceTo(entity.getPos());
            if (dist > targetRange * targetRange)
            {
                continue;
            }

            float damage = getCrystalDamage(explosionCenter, entity);

            crystalBases.add(new CrystalData<>(pos, damage, local));
        }
    }

    public List<CrystalData<BlockPos>> scanCrystalBases()
    {
        crystalBases.clear();
        try
        {
            scanBlocks();
        } catch (Throwable ignored)
        {

        }

        return crystalBases;
    }

    @Override
    protected int getRadius()
    {
        return (int) Math.ceil(autoCrystal.getPlaceRange().getValue());
    }

    private boolean canUseOnBlock(BlockPos pos, BlockState state)
    {
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK))
        {
            return false;
        }

        BlockPos p2 = pos.up();
        BlockState state2 = getBlockState(p2);
        if (autoCrystal.getProtocolPlace().getValue() && !getBlockState(p2.up()).isAir())
        {
            return false;
        }

        if (!state2.isAir() && !state2.isOf(Blocks.FIRE))
        {
            return false;
        } else
        {
            final Box bb = FULL_CRYSTAL_BB;
            List<EntityState> list = getEntitiesBlockingCrystal(bb.offset(p2.getX(), p2.getY(), p2.getZ()));
            return list.isEmpty();
        }
    }

    private List<EntityState> getEntitiesBlockingCrystal(Box box)
    {
        List<EntityState> entities = getOtherEntities(null, box);
        for (EntityState entity : entities)
        {
            final Entity entity1 = entity.getEntity();
            if (entity1 instanceof ExperienceOrbEntity
                    || autoCrystal.getForcePlace().getValue() && entity1 instanceof ItemEntity && entity.getAge() <= 10)
            {
                entities.remove(entity);
            } else if (entity1 instanceof EndCrystalEntity crystal)
            {
                entities.remove(entity);
            }
        }

        return entities;
    }

    private float getCrystalDamage(Vec3d pos, EntityState entity)
    {
        return ExplosionTrace.getDamageToPos(this, pos, entity.getPos(), entity.getBoundingBox(), 12.0f, autoCrystal.getIgnoreTerrain().getValue());
    }
}
