package net.shoreline.client.impl.combat.hole;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.impl.block.AsyncBlockScanner;
import net.shoreline.client.impl.block.AsyncBlockState;
import net.shoreline.client.impl.module.combat.FillerModule;
import net.shoreline.client.impl.module.render.HoleESPModule;
import net.shoreline.client.util.world.BlockUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class HoleScanner extends AsyncBlockScanner
{
    private final Set<BlockPos> visited = new ConcurrentSkipListSet<>();
    private final List<HoleData> safeHoles = new CopyOnWriteArrayList<>();

    @Override
    protected void visit(BlockPos pos, AsyncBlockState asyncState)
    {
        BlockState state = asyncState.getBlockState();
        if (!state.isReplaceable() || !getBlockState(pos.up()).isReplaceable() || !getBlockState(pos.up(2)).isReplaceable())
        {
            return;
        }

        if (getBlockState(pos.down()).isReplaceable() || visited.contains(pos))
        {
            return;
        }

        boolean scanDoubleHoles = HoleESPModule.INSTANCE.shouldGetDoubles() || FillerModule.INSTANCE.shouldGetDoubles();
        boolean scanQuadHoles = HoleESPModule.INSTANCE.shouldGetQuads();

        if (scanQuadHoles && scanQuadHole(pos))
        {
            return;
        }

        if (scanDoubleHoles && (scanDoubleHole(pos, Direction.EAST) || scanDoubleHole(pos, Direction.SOUTH)))
        {
            return;
        }

        scanSingleHole(pos);
    }

    @Override
    protected int getRadius()
    {
        return Math.max(10, (int) Math.ceil(HoleESPModule.INSTANCE.getRange() + 1.0f));
    }

    public List<HoleData> scanHoles()
    {
        visited.clear();
        safeHoles.clear();

        scanBlocks();

        return safeHoles;
    }

    private void scanSingleHole(BlockPos pos)
    {
        boolean hasBedrock = false;
        boolean hasObsidian = false;

        for (Direction dir : Direction.Type.HORIZONTAL)
        {
            BlockState side = getBlockState(pos.offset(dir));
            if (BlockUtil.isUnbreakable(side))
            {
                hasBedrock = true;
            } else if (BlockUtil.isExplosionResistant(side))
            {
                hasObsidian = true;
            } else
            {
                return;
            }
        }

        visited.add(pos);
        safeHoles.add(new HoleData(hasObsidian, hasBedrock, pos));
    }

    private boolean scanDoubleHole(BlockPos pos, Direction axisDir)
    {
        boolean hasBedrock = false;
        boolean hasObsidian = false;

        BlockPos otherPos = pos.offset(axisDir);
        if (!getBlockState(otherPos).isReplaceable() || getBlockState(otherPos.down()).isReplaceable())
        {
            return false;
        }

        for (BlockPos checkPos : new BlockPos[] { pos, otherPos })
        {
            if (visited.contains(checkPos))
            {
                continue;
            }

            for (Direction dir : Direction.Type.HORIZONTAL)
            {
                BlockPos sidePos = checkPos.offset(dir);
                BlockState side = getBlockState(sidePos);
                if (sidePos.equals(pos) || sidePos.equals(otherPos))
                {
                    continue;
                }

                if (BlockUtil.isUnbreakable(side))
                {
                    hasBedrock = true;
                } else if (BlockUtil.isExplosionResistant(side))
                {
                    hasObsidian = true;
                } else
                {
                    return false;
                }
            }
        }

        visited.add(pos);
        visited.add(otherPos);
        safeHoles.add(new HoleData(hasObsidian, hasBedrock, pos, otherPos));
        return true;
    }

    private boolean scanQuadHole(BlockPos pos)
    {
        boolean hasBedrock = false;
        boolean hasObsidian = false;

        BlockPos east = pos.east();
        BlockPos south = pos.south();
        BlockPos southeast = pos.add(1, 0, 1);

        for (BlockPos checkPos : new BlockPos[] { pos, east, south, southeast })
        {
            if (!getBlockState(checkPos).isReplaceable() || getBlockState(checkPos.down()).isReplaceable())
            {
                return false;
            }
        }

        Set<BlockPos> holeBlocks = Set.of(pos, east, south, southeast);
        BlockPos[] wallPositions = {
                pos.north(),
                east.north(),
                south.south(),
                southeast.south(),
                pos.west(),
                south.west(),
                east.east(),
                southeast.east()
        };

        for (BlockPos wallPos : wallPositions)
        {
            if (holeBlocks.contains(wallPos))
            {
                continue;
            }

            BlockState wallState = getBlockState(wallPos);
            if (BlockUtil.isUnbreakable(wallState))
            {
                hasBedrock = true;
            } else if (BlockUtil.isExplosionResistant(wallState))
            {
                hasObsidian = true;
            } else
            {
                return false;
            }
        }

        visited.add(pos);
        visited.add(east);
        visited.add(south);
        visited.add(southeast);
        safeHoles.add(new HoleData(hasObsidian, hasBedrock, pos, east, south, southeast));
        return true;
    }
}

