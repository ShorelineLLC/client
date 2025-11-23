package net.shoreline.client.impl.module.combat.anchor;

import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.block.AsyncBlockState;
import net.shoreline.client.impl.interact.InteractDirection;
import net.shoreline.client.impl.module.combat.AnchorAuraModule;
import net.shoreline.client.impl.module.combat.util.MovementExtrapolation;
import net.shoreline.client.impl.world.AsyncWorldScanner;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.explosion.ExplosionTrace;
import net.shoreline.client.util.entity.EntityUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

@RequiredArgsConstructor
public class AnchorScanner extends AsyncWorldScanner
{
    private final AnchorAuraModule module;
    private final Set<AnchorData> data = new TreeSet<>();

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
        if (!Managers.INTERACT.canPlaceBlock(pos, block) && blockState.isReplaceable())
        {
            return;
        }

        if (blockState.isReplaceable())
        {
            Direction direction = InteractDirection.getInteractDirection(pos, module.isStrictDirection());
            if (direction == null)
            {
                return;
            }
        }

        AnchorData positionData = new AnchorData(pos);
        if (block == Blocks.RESPAWN_ANCHOR)
        {
            positionData.setAnchor(true);
        }
        else if (!blockState.isReplaceable())
        {
            return;
        }

        float selfDamage = getDamage(pos, getLocalEntity().getEntity());
        if (selfDamage > module.getMaxSelfDamageConfig().getValue()
                || getLocalEntity().getTotalHealth() - selfDamage < 0.5f)
        {
            return;
        }

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

            if (damage > entityState.getTotalHealth()) // prioritize lethal positions.
            {
                positionData.setDamage(damage);
                positionData.setTarget((PlayerEntity) entity);
                break;
            }
        }

        if (positionData.getDamage() > module.getMinDamageConfig().getValue())
        {
            data.add(positionData);
        }
    }

    @Override
    protected int getRadius()
    {
        return (int) Math.ceil(module.getRangeConfig().getValue());
    }

    public Collection<AnchorData> getData()
    {
        data.clear();
        scanBlocks();
        return data;
    }

    public float getDamage(BlockPos pos, Entity entity)
    {
        int extrapolation = module.getExtrapolateConfig().getValue();
        Vec3d extrapolatedPos = extrapolation <= 0
                ? entity.getPos()
                : MovementExtrapolation.extrapolatePosition(
                        this,
                        entity.getVelocity(),
                        entity.getBoundingBox(),
                        entity,
                        extrapolation);

        Box boundingBox = entity.getDimensions(entity.getPose()).getBoxAt(extrapolatedPos);
        return ExplosionTrace.getDamageToPos(
                this,
                pos.toCenterPos(),
                extrapolatedPos,
                boundingBox,
                10.0f,
                module.getIgnoreTerrain().getValue(),
                Set.of(pos));
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
