package net.shoreline.client.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class NukerModule extends ToggleModule
{
    Config<NukeMode> modeConfig = register(new EnumConfig<>("Selection", "The nuker selection mode", NukeMode.SPHERE, NukeMode.values()));
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The range to mine blocks", 0.0f, 4.0f, 6.0f));
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The speed to mine blocks", 0.1f, 1.0f, 1.0f));
    Config<Boolean> flattenConfig = register(new BooleanConfig("Flatten", "Only clears above the player y-level", false, () -> modeConfig.getValue() == NukeMode.SPHERE));
    Config<Boolean> doubleBreakConfig = register(new BooleanConfig("DoubleBreak", "Mines two blocks at a time", false));
    Config<Swap> swapConfig = register(new EnumConfig<>("AutoSwap", "Swaps to the best tool once the mining is complete", Swap.SILENT, Swap.values()));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates before mining blocks", false));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Uses grim block breaking speeds", false));
    private final List<BlockPos> miningBlocks = new ArrayList<>();
    private final Queue<BlockPos> selectedBlocks = new LinkedList<>();

    public NukerModule()
    {
        super("Nuker", "Clears nearby blocks", ModuleCategory.WORLD);
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        for (BlockPos blockPos : selectedBlocks)
        {

        }
        while (miningBlocks.size() < (doubleBreakConfig.getValue() ? 2 : 1))
        {
            switch (modeConfig.getValue())
            {
                case SPHERE ->
                {
                    List<BlockPos> sphere = getSphere(mc.player.getPos());
                    BlockPos minePos = null;
                    for (BlockPos blockPos : sphere)
                    {
                        BlockState state = mc.world.getBlockState(blockPos);
                        if (BlastResistantBlocks.isUnbreakable(state.getBlock()) || state.isAir() || !state.getFluidState().isEmpty())
                        {
                            continue;
                        }
                        minePos = blockPos;
                    }
                    if (minePos == null)
                    {
                        return;
                    }
                    mineBlock(minePos);
                }
                case CLICK_SELECT ->
                {
                    if (selectedBlocks.isEmpty())
                    {
                        return;
                    }
                    BlockPos minePos = selectedBlocks.poll();
                    if (minePos == null)
                    {
                        return;
                    }
                    mineBlock(minePos);
                }
            }
        }
    }

    @EventListener
    public void onAttackBlock(AttackBlockEvent event)
    {
        if (modeConfig.getValue() != NukeMode.CLICK_SELECT || mc.player.isCreative() || mc.player.isSpectator())
        {
            return;
        }
        event.cancel();
        // Do not try to break unbreakable blocks
        if (event.getState().getBlock().getHardness() == -1.0f || event.getState().isAir())
        {
            return;
        }
        selectedBlocks.add(event.getPos());
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void mineBlock(BlockPos blockPos)
    {

        miningBlocks.add(blockPos);
    }

    private List<BlockPos> getSphere(Vec3d origin)
    {
        List<BlockPos> sphere = new ArrayList<>();
        double rad = Math.ceil(rangeConfig.getValue());
        for (double x = -rad; x <= rad; ++x)
        {
            for (double y = flattenConfig.getValue() ? 0.0 : -rad; y <= rad; ++y)
            {
                for (double z = -rad; z <= rad; ++z)
                {
                    Vec3i pos = new Vec3i((int) (origin.getX() + x),
                            (int) (origin.getY() + y), (int) (origin.getZ() + z));
                    final BlockPos p = new BlockPos(pos);
                    sphere.add(p);
                }
            }
        }
        return sphere;
    }

    public enum NukeMode
    {
        SPHERE,
        CLICK_SELECT
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }

    public static class NukerData
    {

    }
}
