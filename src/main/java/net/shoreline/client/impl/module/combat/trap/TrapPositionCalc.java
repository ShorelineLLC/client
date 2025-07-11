package net.shoreline.client.impl.module.combat.trap;

import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class TrapPositionCalc
{
    private final List<BlockPos> trapPositions = new CopyOnWriteArrayList<>();

    public void calcTrap(Box boundingBox, TrapSpec trapSpec)
    {
        trapPositions.clear();

        List<BlockPos> origin = BlockPos.stream(boundingBox).map(BlockPos::toImmutable).toList();
        int feetY = (int) boundingBox.minY;
        int bodyY = feetY + 1;

        for (BlockPos blockPos : origin)
        {
            addCoreLayers(trapSpec.getLayers(), origin, blockPos, feetY, bodyY);
        }

        extendLayers(trapSpec, origin, feetY, bodyY);

        trapPositions.sort(Comparator.comparingInt(Vec3i::getY));
    }

    private void addCoreLayers(EnumSet<TrapLayer> layers,
                               List<BlockPos> origin,
                               BlockPos blockPos,
                               int feetY,
                               int bodyY)
    {
        int y = blockPos.getY();
        if (feetY == y)
        {
            if (layers.contains(TrapLayer.FEET))
            {
                extendTrapAroundPos(blockPos, origin, false);
            }
            if (layers.contains(TrapLayer.HEAD))
            {
                trapPositions.add(blockPos.up(2));
            }
        }

        if (bodyY == y)
        {
            if (layers.contains(TrapLayer.BODY))
            {
                extendTrapAroundPos(blockPos, origin, false);
            }
            if (layers.contains(TrapLayer.CRAWL))
            {
                trapPositions.add(blockPos);
            }
        }
    }

    private void extendLayers(TrapSpec spec,
                              List<BlockPos> origin,
                              int feetY,
                              int bodyY)
    {
        for (BlockPos blockPos : trapPositions)
        {
//            if (Managers.MINING.getMiningProgress(blockPos) < 0.75f)
//            {
//                continue;
//            }

            int y = blockPos.getY();
            if (feetY == y && spec.isExtendFeet() || bodyY == y && spec.isExtendBody())
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
