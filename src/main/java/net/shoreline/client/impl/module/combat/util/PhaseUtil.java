package net.shoreline.client.impl.module.combat.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class PhaseUtil
{
    public List<BlockPos> intersectingBlocks(Entity entity)
    {
        Box box = entity.getBoundingBox();
        Set<BlockPos> origin = BlockPos.stream(box)
                .map(BlockPos::toImmutable)
                .collect(Collectors.toCollection(HashSet::new));

        return origin.stream().filter(pos -> !MinecraftClient.getInstance().world.getBlockState(pos).isReplaceable()).toList();
    }

    public boolean isInsideBlock(Entity entity)
    {
        return !intersectingBlocks(entity).isEmpty();
    }

    public boolean isInsideWeb(Entity entity)
    {
        return false;
    }

    public boolean isInsideWall(Entity entity)
    {
        BlockPos blockPos = entity.getBlockPos();
        List<BlockPos> blocks = intersectingBlocks(entity);
        return blocks.stream().anyMatch(p -> p.getY() == blockPos.getY())
                && blocks.stream().anyMatch(p -> p.getY() == blockPos.getY() + 1);
    }
}
