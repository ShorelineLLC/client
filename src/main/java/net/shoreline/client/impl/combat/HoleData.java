package net.shoreline.client.impl.combat;

import lombok.Data;
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

    public Box getBoundingBox()
    {
        return new Box(holePos[0]);
    }
}
