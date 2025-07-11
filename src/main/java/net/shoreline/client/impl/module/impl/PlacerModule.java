package net.shoreline.client.impl.module.impl;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.AnticheatModule;

import java.util.ArrayList;
import java.util.List;

public class PlacerModule extends Toggleable
{
    protected final AnticheatModule anticheat = AnticheatModule.INSTANCE;

    public PlacerModule(String name, String description, GuiCategory category) {
        super(name, description, category);
    }

    public PlacerModule(final String name,
                        final String[] nameAliases,
                        final String description,
                        final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    protected List<BlockPos> getPlacements(Block block, List<BlockPos> posList, double range)
    {
        final List<BlockPos> placements = new ArrayList<>();
        if (posList.isEmpty())
        {
            return placements;
        }

        for (BlockPos blockPos : posList)
        {
            double dist = mc.player.squaredDistanceTo(blockPos.toCenterPos());
            if (dist > range * range)
            {
                continue;
            }

            if (!mc.world.getBlockState(blockPos).isReplaceable())
            {
                continue;
            }

            if (!Managers.INTERACT.canPlaceBlock(blockPos, block))
            {
                continue;
            }

            placements.add(blockPos);
        }

        return placements;
    }
}
