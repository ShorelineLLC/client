package net.shoreline.client.impl.module.combat.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.shoreline.client.util.world.BlockUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class PhaseUtil
{
    public List<BlockPos> intersectingBlocks(Box box)
    {
        World world = MinecraftClient.getInstance().world;
        VoxelShape entityShape = VoxelShapes.cuboid(box);

        int minX = MathHelper.floor(box.minX);
        int minY = MathHelper.floor(box.minY);
        int minZ = MathHelper.floor(box.minZ);
        int maxX = MathHelper.floor(box.maxX);
        int maxY = MathHelper.floor(box.maxY);
        int maxZ = MathHelper.floor(box.maxZ);

        List<BlockPos> out = new ArrayList<>();
        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++)
            {
                for (int z = minZ; z <= maxZ; z++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    VoxelShape collisionShape = world.getBlockState(pos).getCollisionShape(world, pos);
                    if (collisionShape.isEmpty())
                    {
                        continue;
                    }

                    if (VoxelShapes.matchesAnywhere(collisionShape.offset(x, y, z), entityShape, BooleanBiFunction.AND))
                    {
                        out.add(pos.toImmutable());
                    }
                }
            }
        }

        return out;
    }

    public boolean isInsideBedrock(Entity entity)
    {
        return getFeetBlocks(entity).stream().anyMatch(BlockUtil::isUnbreakable);
    }

    public boolean isInsideBedrockWall(Entity entity)
    {
        return getWallBlocks(entity).stream().anyMatch(BlockUtil::isUnbreakable);
    }

    public boolean isInsideBlock(Entity entity)
    {
        return !getFeetBlocks(entity).isEmpty();
    }

    public boolean isInsideWall(Entity entity)
    {
        return !getWallBlocks(entity).isEmpty();
    }

    public List<BlockPos> getFeetBlocks(Entity entity)
    {
        if (entity.isCrawling())
        {
            return new ArrayList<>();
        }

        Box box = entity.getBoundingBox();
        Box feetBox = new Box(box.minX, box.minY, box.minZ, box.maxX, box.minY + 0.1, box.maxZ);
        return intersectingBlocks(feetBox);
    }

    public List<BlockPos> getWallBlocks(Entity entity)
    {
        if (entity.isCrawling())
        {
            return new ArrayList<>();
        }

        Box box = entity.getBoundingBox();
        Box bodyBox = new Box(box.minX, box.minY + 1.0, box.minZ, box.maxX, box.minY + 1.1, box.maxZ);
        return intersectingBlocks(bodyBox);
    }

    public boolean isInsideWeb(Entity entity)
    {
        return false;
    }
}
