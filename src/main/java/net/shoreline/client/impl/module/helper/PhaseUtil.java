package net.shoreline.client.impl.module.helper;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

@UtilityClass
public class PhaseUtil
{
    public List<BlockPos> intersectingBlocks(Entity entity)
    {
        return BlockPos.stream(entity.getBoundingBox())
                .filter(pos -> !MinecraftClient.getInstance().world.getBlockState(pos).isReplaceable())
                .toList();
    }

    public boolean isInsideBlock(Entity entity)
    {
        return !intersectingBlocks(entity).isEmpty();
    }

    public boolean isInsideWall(Entity entity)
    {
        BlockPos blockPos = entity.getBlockPos();
        List<BlockPos> blocks = intersectingBlocks(entity);
        return blocks.stream().anyMatch(p -> p.getY() == blockPos.getY())
                && blocks.stream().anyMatch(p -> p.getY() == blockPos.up().getY());
    }
}
