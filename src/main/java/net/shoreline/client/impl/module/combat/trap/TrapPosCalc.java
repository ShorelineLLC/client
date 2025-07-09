package net.shoreline.client.impl.module.combat.trap;

import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TrapPosCalc
{
    private final List<BlockPos> trapPositions = new ArrayList<>();

    public void calcTrap(Box boundingBox,
                         boolean feet,
                         boolean body,
                         boolean head,
                         boolean crawl)
    {
        calcTrap(boundingBox, feet, body, head, crawl, false, false);
    }

    public void calcTrap(Box boundingBox,
                         boolean feet,
                         boolean body,
                         boolean head)
    {
        calcTrap(boundingBox, feet, body, head, false, false, false);
    }

    public void calcTrap(Box boundingBox,
                         boolean feet,
                         boolean body,
                         boolean head,
                         boolean crawl,
                         boolean extendFeet,
                         boolean extendBody)
    {
        trapPositions.clear();

        List<BlockPos> origin = BlockPos.stream(boundingBox).toList();

        int feetY = (int) Math.round(boundingBox.minY);
        int bodyY = feetY + 1;

        for (BlockPos blockPos : origin)
        {
            int y = blockPos.getY();
            if (feetY == y)
            {
                if (feet)
                {
                    extendTrapAroundPos(blockPos, origin, false);
                }
                if (head)
                {
                    trapPositions.add(blockPos.up(2));
                }
            }

            if (bodyY == y)
            {
                if (body)
                {
                    extendTrapAroundPos(blockPos, origin, false);
                }
                if (crawl)
                {
                    trapPositions.add(blockPos);
                }
            }
        }

        for (BlockPos blockPos : trapPositions)
        {
            int y = blockPos.getY();
            if (feetY == y && extendFeet || bodyY == y && extendBody)
            {
                extendTrapAroundPos(blockPos, origin, true);
            }
        }
    }

    private void extendTrapAroundPos(BlockPos pos,
                                     List<BlockPos> origin,
                                     boolean vertical)
    {
        for (Direction direction : Direction.values())
        {
            if (!vertical && direction.getAxis().isVertical())
            {
                continue;
            }

            BlockPos extend = pos.offset(direction);
            if (origin.contains(extend))
            {
                continue;
            }

            trapPositions.add(extend);
        }
    }
}
