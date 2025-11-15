package net.shoreline.client.impl.module.combat.anchor;

import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.block.AsyncBlockState;
import net.shoreline.client.impl.module.combat.AnchorAuraModule;
import net.shoreline.client.impl.world.AsyncWorldScanner;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.explosion.ExplosionTrace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

@RequiredArgsConstructor
public class AnchorScanner extends AsyncWorldScanner
{
    private final AnchorAuraModule module;
    private Collection<AnchorPositionData> data = new TreeSet<>();

    @Override
    protected void visit(BlockPos pos, AsyncBlockState state)
    {
        if (checkBlocking(pos))
        {
            return;
        }

        double range = pos.getSquaredDistance(getLocalEntity().getPos());
        if (range > MathHelper.square(module.getAnchorRangeConfig().getValue()))
        {
            return;
        }

        BlockState blockState = getBlockState(pos);
        Block block = blockState.getBlock();
        if (!Managers.INTERACT.canPlaceBlock(pos, block))
        {
            return;
        }

        AnchorPositionData positionData = new AnchorPositionData(pos);
        if (block == Blocks.RESPAWN_ANCHOR)
        {
            positionData.setAnchor(true);
        }
        else if (!blockState.isReplaceable())
        {
            return;
        }

        float selfDamage = getDamage(pos, getLocalEntity().getEntity());
        positionData.setSelfDamage(selfDamage);

        for (EntityState entityState : getEntities())
        {
            Entity entity = entityState.getEntity();
            if (!(entity instanceof PlayerEntity) || entity == getLocalEntity().getEntity())
            {
                continue;
            }

            double targetRange = entity.squaredDistanceTo(getLocalEntity().getEntity());
            if (targetRange > MathHelper.square(module.getRangeConfig().getValue()))
            {
                continue;
            }

            float damage = getDamage(pos, entity);
            if (damage > positionData.getDamage())
            {
                positionData.setDamage(damage);
                positionData.setTarget((PlayerEntity) entity);
            }
        }

        data.add(positionData);
    }

    @Override
    protected int getRadius()
    {
        return (int) Math.ceil(module.getRangeConfig().getValue());
    }

    public Collection<AnchorPositionData> getData()
    {
        data.clear();
        scanBlocks();
        return data;
    }

    public float getDamage(BlockPos pos, Entity entity)
    {
        return ExplosionTrace.getDamageToPos(
                this,
                pos.toCenterPos(),
                entity.getPos(),
                entity.getBoundingBox(),
                10.0f,
                module.getIgnoreTerrain().getValue());
    }

    public boolean checkBlocking(BlockPos pos)
    {
        for (EntityState state : getOtherEntities(null, new Box(pos)))
        {
            Entity entity = state.getEntity();
            if (entity instanceof ExperienceOrbEntity || entity instanceof ItemEntity)
            {
                continue;
            }

            return true;
        }

        return false;
    }
}
