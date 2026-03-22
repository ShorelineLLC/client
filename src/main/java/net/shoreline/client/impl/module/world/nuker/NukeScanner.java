package net.shoreline.client.impl.module.world.nuker;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.impl.block.AsyncBlockScanner;
import net.shoreline.client.impl.block.AsyncBlockState;
import net.shoreline.client.impl.module.world.NukerModule;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter
public class NukeScanner extends AsyncBlockScanner
{
    private final NukerModule module;
    private final ConcurrentSkipListSet<BlockPos> result;

    public NukeScanner(NukerModule module)
    {
        this.module = module;
        this.result = new ConcurrentSkipListSet<>(Comparator.comparing(pos ->
                -MinecraftClient.getInstance().player.squaredDistanceTo(pos.toCenterPos())));
    }

    @Override
    protected void visit(BlockPos pos, AsyncBlockState state)
    {
        if (!module.isValid(pos, state.getBlockState()))
        {
            return;
        }

        double dist = MinecraftClient.getInstance().player.
                squaredDistanceTo(pos.toCenterPos());
        if (dist > MathHelper.square(module.getRange()))
        {
            return;
        }

        result.add(pos);
    }

    public Collection<BlockPos> scanAndGet()
    {
        result.clear();
        scanBlocks();
        return result;
    }

    @Override
    protected int getRadius()
    {
        return (int) Math.ceil(module.getRange() + 1.0f);
    }
}
