package net.shoreline.client.impl.combat;

import lombok.Data;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

@Data
public class HoleData
{
    private final HoleBlockType blockType;
    private final BlockPos[] holePos;

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
    }

    public double squaredDistanceTo(Entity entity)
    {
        return entity.getPos().squaredDistanceTo(getBoundingBox(1.0f).getCenter());
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
