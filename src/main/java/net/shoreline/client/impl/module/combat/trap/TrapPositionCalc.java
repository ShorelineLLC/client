package net.shoreline.client.impl.module.combat.trap;

import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.shoreline.client.impl.Managers;

import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Getter
public class TrapPositionCalc
{
    private final Comparator<Vec3i> trapOrdering;
    private final ConcurrentNavigableMap<BlockPos, TrapLayer> trapPositions;

    public TrapPositionCalc()
    {
        this(Comparator.comparingInt(Vec3i::getY)
                .thenComparingInt(Vec3i::getX)
                .thenComparingInt(Vec3i::getZ));
    }

    public TrapPositionCalc(Comparator<Vec3i> trapOrdering)
    {
        this.trapOrdering = trapOrdering;
        this.trapPositions = new ConcurrentSkipListMap<>(trapOrdering);
    }

    public void calcTrap(Box boundingBox, TrapSpec trapSpec)
    {
        trapPositions.clear();

        Set<BlockPos> origin = BlockPos.stream(boundingBox)
                .map(BlockPos::toImmutable)
                .collect(Collectors.toCollection(HashSet::new));

        int feetY = (int) boundingBox.minY;
        int bodyY = feetY + 1;

        for (BlockPos blockPos : origin)
        {
            addCoreLayers(trapSpec.getLayers(), origin, blockPos, feetY, bodyY);
        }

        extendLayers(trapSpec, origin, feetY, bodyY);
    }

    private void addCoreLayers(EnumSet<TrapLayer> layers,
                               Set<BlockPos> origin,
                               BlockPos blockPos,
                               int feetY,
                               int bodyY)
    {
        if (layers.contains(TrapLayer.FEET_INTERSECT) || layers.contains(TrapLayer.BODY_INTERSECT))
        {
            for (BlockPos pos : origin)
            {
                int inY = pos.getY();
                if (feetY == inY)
                {
                    trapPositions.put(pos, TrapLayer.FEET_INTERSECT);
                } else if (bodyY == inY)
                {
                    trapPositions.put(pos, TrapLayer.BODY_INTERSECT);
                }
            }
        }

        int y = blockPos.getY();
        if (feetY == y)
        {
            if (layers.contains(TrapLayer.FEET))
            {
                extendTrapAroundPos(blockPos, origin, TrapLayer.FEET, false);
            }

            if (layers.contains(TrapLayer.FLOOR))
            {
                trapPositions.put(blockPos.down(), TrapLayer.FLOOR);
            }

            if (layers.contains(TrapLayer.CEILING))
            {
                trapPositions.put(blockPos.up(2), TrapLayer.CEILING);
            }
        }

        if (bodyY == y && layers.contains(TrapLayer.BODY))
        {
            extendTrapAroundPos(blockPos, origin, TrapLayer.BODY, false);
        }
    }

    private void extendLayers(TrapSpec spec,
                              Set<BlockPos> origin,
                              int feetY,
                              int bodyY)
    {
        for (BlockPos blockPos : trapPositions.keySet())
        {
            if (Managers.MINING.getMiningProgress(blockPos) < 0.5f)
            {
                continue;
            }

            int y = blockPos.getY();
            if (feetY == y && spec.isExtendFeet())
            {
                extendTrapAroundPos(blockPos, origin, TrapLayer.EXTEND_FEET, true);
            } else if (bodyY == y && spec.isExtendBody())
            {
                extendTrapAroundPos(blockPos, origin, TrapLayer.EXTEND_BODY, true);
            }
        }
    }

    private void extendTrapAroundPos(BlockPos pos,
                                     Set<BlockPos> origin,
                                     TrapLayer trapLayer,
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

            trapPositions.put(extend, trapLayer);
        }
    }

    public List<Map.Entry<BlockPos, TrapLayer>> entriesSortedByLayer(TrapLayer... layerOrder)
    {
        List<Map.Entry<BlockPos, TrapLayer>> list = new ArrayList<>(trapPositions.entrySet());
        if (layerOrder == null || layerOrder.length == 0)
        {
            list.sort(Map.Entry.comparingByKey(trapOrdering));
            return list;
        }

        EnumMap<TrapLayer, Integer> rank = new EnumMap<>(TrapLayer.class);
        for (int i = 0; i < layerOrder.length; i++)
        {
            rank.put(layerOrder[i], i);
        }

        list.sort(
                Comparator.<Map.Entry<BlockPos, TrapLayer>>comparingInt(e -> rank.getOrDefault(e.getValue(), Integer.MAX_VALUE))
                        .thenComparing(Map.Entry::getKey, trapOrdering)
        );

        return list;
    }

    public TrapLayer getLayerType(BlockPos blockPos)
    {
        return trapPositions.get(blockPos);
    }

    public ConcurrentNavigableMap<BlockPos, TrapLayer> getTrapPositionLayers()
    {
        return trapPositions;
    }

    public NavigableSet<BlockPos> getTrapPositions()
    {
        return trapPositions.keySet();
    }
}
