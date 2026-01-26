package net.shoreline.client.impl.combat.hole;

import lombok.Data;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Data
public class HoleData
{
    private final HoleBlockType blockType;
    private final BlockPos[] holePos;
    /** If this hole is a 2x1 or a 2x2. */
    private final boolean big;

    public HoleData(boolean hasObsidian, boolean hasBedrock, BlockPos... holePos)
    {
        if (hasObsidian && hasBedrock)
        {
            this.blockType = HoleBlockType.MIXED;
        } else if (hasObsidian)
        {
            this.blockType = HoleBlockType.OBSIDIAN;
        } else if (hasBedrock)
        {
            this.blockType = HoleBlockType.BEDROCK;
        } else
        {
            this.blockType = HoleBlockType.OBSIDIAN;
        }

        this.holePos = holePos;
        this.big = holePos.length > 1;
    }

    public boolean checkRange(Vec3d pos, float range)
    {
        return squaredDistanceTo(pos) > MathHelper.square(range);
    }

    public double squaredDistanceTo(Entity entity)
    {
        return squaredDistanceTo(entity.getPos());
    }

    public double squaredDistanceTo(Vec3d pos)
    {
        return pos.squaredDistanceTo(getBoundingBox(1.0f).getCenter());
    }

    public Box getBoundingBox(double height)
    {
        final Box box1 = new Box(holePos[0]);
        double minX = box1.minX;
        double minY = box1.minY;
        double minZ = box1.minZ;
        double maxX = box1.maxX;
        double maxZ = box1.maxZ;
        for (BlockPos blockPos : holePos)
        {
            Box box = new Box(blockPos);

            if (box.minX < minX)
            {
                minX = box.minX;
            }
            if (box.minZ < minZ)
            {
                minZ = box.minZ;
            }
            if (box.maxX > maxX)
            {
                maxX = box.maxX;
            }
            if (box.maxZ > maxZ)
            {
                maxZ = box.maxZ;
            }
        }

        return new Box(minX, minY, minZ, maxX, minY + height, maxZ);
    }
}
