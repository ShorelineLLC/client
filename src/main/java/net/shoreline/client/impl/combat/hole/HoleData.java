package net.shoreline.client.impl.combat.hole;

import lombok.Data;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.util.world.BlockUtil;

import java.util.List;

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
        return BlockUtil.getBoundingBox(List.of(holePos), height);
    }
}
