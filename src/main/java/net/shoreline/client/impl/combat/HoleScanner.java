package net.shoreline.client.impl.combat;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.impl.async.AsyncBlockScanner;
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
    protected void visit(BlockPos pos, BlockState state)
    {
        if (!state.isReplaceable() || visited.contains(pos))
        {
            return;
        }

        BlockPos down = pos.down();
        if (getBlockState(down).isReplaceable())
        {
            return;
        }

        boolean hasObsidian = false;
        boolean hasBedrock = false;

        boolean layer1 = true;
        for (Direction direction : Direction.values())
        {
            if (!direction.getAxis().isHorizontal())
            {
                continue;
            }

            BlockPos off = pos.offset(direction);
            BlockState state1 = getBlockState(off);
            if (BlockUtil.isUnbreakable(state1))
            {
                hasBedrock = true;
            } else if (BlockUtil.isExplosionResistant(state1))
            {
                hasObsidian = true;
            } else
            {
                layer1 = false;
                break;
            }
        }

        if (layer1)
        {
            visited.add(pos);
            safeHoles.add(new HoleData(hasObsidian, hasBedrock, pos));
        }
    }

    @Override
    protected int getRadius()
    {
        return Math.max(10, (int) HoleESPModule.INSTANCE.getRange());
    }

    public List<HoleData> scanHoles()
    {
        visited.clear();
        safeHoles.clear();

        try
        {
            scanSphere();
        } catch (Throwable t)
        {
            t.printStackTrace();
        }

        return safeHoles;
    }
}

